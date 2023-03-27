package com.hhz.ifttt.functions.v3

import java.text.SimpleDateFormat
import java.{lang, util}
import java.util.Date

import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}
import com.google.gson.Gson
import com.googlecode.aviator.{AviatorEvaluator, Expression}
import com.hhz.ifttt.pojo.Pojos
import com.hhz.ifttt.pojo.Pojos.{OutLog, PatternResult, Rule, RuleEvent}
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, StateTtlConfig, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.time.Time
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector

import scala.tools.nsc.doc.html.page.JSONObject

/**
 * @program ifttt 
 * @description: 事件匹配
 * @author: zhangyinghao 
 * @create: 2023/01/09 16:25 
 **/
class CompletePatternProcessFunction extends KeyedProcessFunction[String, (String, RuleEvent), String] {
  // 触发时间
  var triggerTimeValueState: ValueState[Long] = _

  // 触发时间
  var valueState: ValueState[String] = _

  // 次数缓存
  var cntMapState: MapState[Int, Int] = _

  override def open(parameters: Configuration): Unit = {
    // 状态保存时间
    val ttlConfig: StateTtlConfig = StateTtlConfig
      .newBuilder(Time.days(1))
      .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite) // 仅在创建和写入时更新
      .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired) // 读取时也更新
      .build
    val eventTimeValueStateDesc: ValueStateDescriptor[Long] = new ValueStateDescriptor[Long]("eventTimeValueState", classOf[Long])
    eventTimeValueStateDesc.enableTimeToLive(ttlConfig)
    triggerTimeValueState = getRuntimeContext.getState(eventTimeValueStateDesc)


    val cntMapStateDesc: MapStateDescriptor[Int, Int] = new MapStateDescriptor[Int, Int]("cntMapState", classOf[Int], classOf[Int])
    cntMapStateDesc.enableTimeToLive(ttlConfig)
    cntMapState = getRuntimeContext.getMapState(cntMapStateDesc)


    val valueStateDesc: ValueStateDescriptor[String] = new ValueStateDescriptor[String]("valueState", classOf[String])
    valueStateDesc.enableTimeToLive(ttlConfig)
    valueState = getRuntimeContext.getState(valueStateDesc)
  }


  override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[String, (String, RuleEvent), String]#OnTimerContext, out: Collector[String]): Unit = {

    try{


      val value: String = valueState.value()
      if (timestamp == triggerTimeValueState.value() * 1000 && valueState.value() == null) {
        triggerTimeValueState.update(0L)
      } else {
        val ruleEvent: JSONObject = JSON.parseObject(value)
        val array: JSONArray = new JSONArray()
        // 判断是否经过用户画像
        val map: util.HashMap[String, String] = new util.HashMap[String, String](1)
        val rule: JSONObject = ruleEvent.getJSONObject("rule")
        val groupId: String = rule.getString("groupId")
        val uid: String = ruleEvent.getString("uid")
        val data: JSONObject = ruleEvent.getJSONObject("data")
        try {
          data.put("params", JSON.parseObject(data.getString("params")))
          data.put("stat_sign", JSON.parseObject(data.getString("stat_sign")))
        } catch {
          case e: Exception => {
            val nObject: JSONObject = new JSONObject()
            nObject.put("ps", data.getString("params"))
            nObject.put("ifttt_parse_error_reason", e.getMessage)
            data.put("params", nObject)
          }
        }

        array.add(data)
        map.put("begin", array.toJSONString)
        val result: String = new Gson().toJson(PatternResult("ifttt_task_" + groupId, uid, new Date().toString, map, ""))

        // 判断发送至哪里
        ctx.output(Pojos.iftttOutLog, OutLog(uid, data.getString("event"), StringEscapeUtils.unescapeJson(value), "用户已完成事件，匹配数据已下发 ", System.currentTimeMillis()))
        out.collect(result)
        valueState.update(null)
        if (rule.getLong("cacheTime") == 0 && rule.getLong("delayedTrigger") == 0) {
          triggerTimeValueState.update(0L)
        }
        valueState.clear()
      }

    }catch{
      case e: Exception =>
        e.printStackTrace()
        valueState.update(null)
        triggerTimeValueState.update(0L)
        valueState.clear()
    }
  }


  private val sdf: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  override def processElement(value: (String, RuleEvent), ctx: KeyedProcessFunction[String, (String, RuleEvent), String]#Context, out: Collector[String]): Unit = {
    val ruleEvent: RuleEvent = value._2
    val triggerTime: Long = triggerTimeValueState.value()
    if (value._2.data.contains("ifttt_clear_state")) {
      ctx.timerService().deleteProcessingTimeTimer(triggerTime)
      ctx.timerService().deleteEventTimeTimer(triggerTime-(ruleEvent.rule.cacheTime*1000))
      valueState.update(null)
      triggerTimeValueState.update(0L)
      valueState.clear()
      // 触发过了
      ctx.output(Pojos.iftttOutLog, OutLog(ruleEvent.uid, "状态清除",
        new Gson().toJson(value), s"{任务: ${ruleEvent.rule.groupId} 状态清除", System.currentTimeMillis()/1000))
    } else {
      val data: JSONObject = JSON.parseObject(ruleEvent.data)
      val event: String = data.getString("event")
      val ruleEventStr: String = new Gson().toJson(ruleEvent)
      val currTime: Long = System.currentTimeMillis() / 1000
      try {
        // 判断是否已经触发过当前任务， 还未过滤
        if (triggerTime == 0L) {
          // 获取表达式
          val ex: Expression = AviatorEvaluator.compile(ruleEvent.rule.patternStr, true)
          val bool: Boolean = checkPattern(ruleEvent.rule, ex, data, ctx)
          if (bool) {
            valueState.update(new Gson().toJson(value._2))
            registerTimeTimer(data, ruleEvent.rule, ctx)
            ctx.output(Pojos.iftttOutLog, OutLog(ruleEvent.uid, event, ruleEventStr, s"用户已完成事件, 注册触发器: ${new Date(triggerTimeValueState.value())}, 其中延迟触发: ${ruleEvent.rule.delayedTrigger} 秒", currTime))
          }
        } else {
          // 触发过了
          ctx.output(Pojos.iftttOutLog, OutLog(ruleEvent.uid, event,
            ruleEventStr, s"当前任务用户已在触发未过期，过期时间【${sdf.format(new Date(triggerTime))}】", currTime))
        }
      } catch {
        case e: Exception => {
          // 触发过了
          ctx.output(Pojos.iftttOutLog, OutLog(ruleEvent.uid, event,
            ruleEventStr, s"CompletePatternProcessFunction解析匹配出错, msg: ${e.getMessage}", currTime))
        }
      }
    }

  }

  /**
   * 注册触发器
   */
  def registerTimeTimer(data: JSONObject, rule: Rule, ctx: KeyedProcessFunction[String, (String, RuleEvent), String]#Context): Unit = {
    // 用户最后触发事件 时间
    val time: Long = data.getLong("report_time").longValue()
    // 注册时间戳      事件时间  + 用户触发频率时间 + 用户延迟触发时间
    var timerTime: Long = time + rule.delayedTrigger

    // 1.延迟触发器   println("注册延迟触发触发器定时器：" + new Date(timerTime * 1000L))
    ctx.timerService().registerProcessingTimeTimer(timerTime * 1000)

    // 2.注册事件缓存触发
    // 更新timeVaule 事件时间 + 触发频率时间 + 延迟触发时间
    timerTime = timerTime+rule.cacheTime
    triggerTimeValueState.update(timerTime * 1000)
    ctx.timerService().registerProcessingTimeTimer(timerTime * 1000)

  }


  /**
   * 规则匹配
   *
   * @param rule       规则id
   * @param expression 规则串匹配
   * @param data       数据
   * @param ctx        上下文- 用于侧输出
   * @return
   */
  def checkPattern(rule: Rule, expression: Expression, data: JSONObject, ctx: KeyedProcessFunction[String, (String, RuleEvent), String]#Context): Boolean = {
    var currCntFlag: Boolean = true
    try {
      val params: JSONObject = data.getJSONObject("params")
      data.put("params", params)


      val keysToCopy: Seq[String] = Seq("user", "content", "brand", "bs")

      for (key <- keysToCopy) {
        val value = data.getJSONObject(key)
        if (value != null) {
          data.put(key, value)
        }
      }

      if (expression != null) {
        val flag: Boolean = expression.execute(data).toString.toBoolean
        if (flag) {
          val ruleCnt: Int = rule.cnt
          if (ruleCnt != 1) {
            val userCompleteCnt: Int = cntMapState.get(rule.ruleId)
            // 用户首次完成
            if (userCompleteCnt == 0) {
              // 注册完成触发器， 默认1天时间
              // todo 注册完成触发器
            }
            var cnt: Int = userCompleteCnt + 1
            // 次数不一致，说明还没有完成
            if (ruleCnt > cnt) {
              cntMapState.put(rule.ruleId, cnt)
              currCntFlag = false
              ctx.output(Pojos.iftttOutLog, OutLog(data.getString("uid"), data.getString("event"), data.toJSONString, String.format("用户未达规则次数【%s】, 当前次数：【%s】",
                ruleCnt.toString, cnt.toString), System.currentTimeMillis() / 1000))
            } else {
              cntMapState.remove(rule.ruleId)
            }
          }
        } else {
          ctx.output(Pojos.iftttOutLog, OutLog(data.getString("uid"), data.getString("event"), data.toJSONString, "patternStr匹配不成功", System.currentTimeMillis() / 1000))
          currCntFlag = false
        }
      }
      currCntFlag
    } catch {
      case e: Exception => {
        ctx.output(Pojos.iftttOutLog, OutLog(data.getString("uid"), data.getString("event"), data.toJSONString, String.format("规则匹配发生异常, msg: %s", e.getMessage), System.currentTimeMillis() / 1000))
        currCntFlag = false
        currCntFlag
      }
    }


  }
}
