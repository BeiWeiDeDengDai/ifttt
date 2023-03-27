package com.hhz.ifttt.functions

import java.{lang, util}
import java.util.Date

import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}
import com.google.gson.Gson
import com.googlecode.aviator.{AviatorEvaluator, BaseExpression, ClassExpression, Expression}
import com.hhz.ifttt.pattern.RulePatternHandler
import com.hhz.ifttt.pojo.{Constants, Pojos}
import com.hhz.ifttt.pojo.Pojos.{PatternResult, Rule, RuleEvent, UserTask}
import org.apache.flink.api.common.state.{BroadcastState, MapState, MapStateDescriptor, StateTtlConfig, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.time.Time
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}

/**
 * @program ifttt 
 * @description: 事件匹配
 * @author: zyh 
 * @create: 2021/10/19 15:19 
 **/
class RulePatternKeyedProcessFunction extends KeyedBroadcastProcessFunction[String, RuleEvent, Rule, String] {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  // 判断时间是否完成
  var eventValueState: ValueState[Long] = _

  // 次数缓存
  var cntMapState: MapState[String, Int] = _


  // 具体值
  var valueState: ValueState[String] = _

  override def open(parameters: Configuration): Unit = {
    // 状态保存时间
    val ttlConfig: StateTtlConfig = StateTtlConfig
      .newBuilder(Time.days(1))
      .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite) // 仅在创建和写入时更新
      .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired) // 读取时也更新
      .build
    val eventTimeValueStateDesc: ValueStateDescriptor[Long] = new ValueStateDescriptor[Long]("eventTimeValueState", classOf[Long])
    eventTimeValueStateDesc.enableTimeToLive(ttlConfig)
    eventValueState = getRuntimeContext.getState(eventTimeValueStateDesc)


    val cntMapStateDesc: MapStateDescriptor[String, Int] = new MapStateDescriptor[String, Int]("cntMapState", classOf[String], classOf[Int])
    cntMapStateDesc.enableTimeToLive(ttlConfig)
    cntMapState = getRuntimeContext.getMapState(cntMapStateDesc)


    val valueStateDesc: ValueStateDescriptor[String] = new ValueStateDescriptor[String]("valueState", classOf[String])
    valueStateDesc.enableTimeToLive(ttlConfig)
    valueState = getRuntimeContext.getState(valueStateDesc)
  }

  override def close(): Unit = super.close()

  /**
   * 触发器逻辑
   *
   * @param timestamp
   * @param ctx
   * @param out
   */
  override def onTimer(timestamp: Long, ctx: KeyedBroadcastProcessFunction[String, RuleEvent, Rule, String]#OnTimerContext, out: Collector[String]): Unit = {




    try {
      // 如果当前定时器时间戳 为用户频率时间戳 则将key置空
      if (timestamp == eventValueState.value() * 1000 && valueState.value() == null) {
        eventValueState.clear()
      } else {
        // 可能两个定时器同时进来 (仅测试的时候会出现这个情况  cacheTime 与 triggerTime 均为0)
        if (eventValueState.value() != null) {
          // 正常触发器发送数据
          val value: String = valueState.value()
          // 定时发送触发数据
          if (value != null) {
            val ruleEvent: JSONObject = JSON.parseObject(value)
            val array: JSONArray = new JSONArray()
            // 判断是否经过用户画像
            val map: util.HashMap[String, String] = new util.HashMap[String, String](1)
            val rule: JSONObject = ruleEvent.getJSONObject("rule")
            val groupId: String = rule.getString("groupId")
            val uid: String = ruleEvent.getString("uid")
            val isUserProfile: lang.Boolean = rule.getBoolean("isUserProfile")
            val data: JSONObject = ruleEvent.getJSONObject("data")
            try {
              data.put("params", JSON.parseObject(data.getString("params")))
            } catch {
              case e: Exception=>{
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
            if (isUserProfile) {
              ctx.output(RulePatternHandler.userProfileOutputTag, new Gson().toJson(UserTask(rule.getString("ruleId"),
                groupId, uid, System.currentTimeMillis() / 1000, value)))
            } else {
              out.collect(result)
            }

            // 置空
            valueState.update(null)
            cntMapState.clear()
            // 如果 cacheTime + triggerTime都为0 置空 业务漏洞 如果不做的话 会导致永远触发不了
            if (rule.getLong("cacheTime") == 0 && rule.getLong("delayedTrigger") == 0) {
              eventValueState.clear()
            }
          }
        }
      }
    } catch {
      case e: Exception => {
        logger.error("onTimer error, msg:" + e.getMessage + ", value" + valueState.value())
        e.printStackTrace()
      }
    }


  }

  override def processElement(ruleEvent: RuleEvent, ctx: KeyedBroadcastProcessFunction[String, RuleEvent, Rule, String]#ReadOnlyContext, out: Collector[String]): Unit = {
    val expression: String = ctx.getBroadcastState(Pojos.expressionMapDesc).get(ruleEvent.rule.ruleId.toString)
    val data: JSONObject = JSON.parseObject(ruleEvent.data)
    var flag: Boolean = false
    try {
      val ex: Expression = AviatorEvaluator.compile(expression, true)
      ruleEvent.rule.pushType match {

        /*---------------------------------- A事件判断 -------------------------------------------------*/
        case 1 => {
          // 判断A是否完成
          flag = checkPatternData(ruleEvent, ex, data)
          if (flag) {
            registerTimeTimer(data, ruleEvent, ctx, 1)
          }
        }


        /*---------------------------------- 完成A未完成B事件判断 -------------------------------------------------*/
        case 2 => {
          // 完成A事件
          if (ruleEvent.rule.eventType == 1) {
            flag = checkPatternData(ruleEvent, ex, data)
            // 完成 A事件后 直接基于
            //  1. 更新A字段
            //  2. 事件时间 + 时间间隔 注册触发器
            if (flag) {
              registerTimeTimer(data, ruleEvent, ctx, 2)
            }
          } else {
            // B事件
            // 首先判断 eventValueState是否为空， 不为空说明此事件还没完成，据需判断是否满足b 如果满足 则删除触发器
            if (eventValueState.value() != null || eventValueState.value() != -1) {
              flag = checkPatternData(ruleEvent, ex, data)
              // 如果B事件完成了
              if (flag) {
                // 删除触发器
//                println("删除触发器" + new Date(eventValueState.value() * 1000L))
                ctx.timerService().deleteProcessingTimeTimer(eventValueState.value() * 1000)
                eventValueState.clear()
              }
            }
          }
        }
        /*---------------------------------- 浏览页面事件判断 -------------------------------------------------*/
        case 3 => {
          // 判断浏览的页面是否是当前类型
          val params: JSONObject = data.getJSONObject("params")
          val startTime: lang.Long = params.getLong("start_time")
          // 过滤掉 startTime <= 0的数
          if (startTime > 0) {
            flag = checkPatternData(ruleEvent, ex, data)
            if (flag) {
              // 拿取当前访问的时长
              val browseTime: Int = cntMapState.get(ruleEvent.rule.ruleId.toString)
              val diffTime: Integer = params.getInteger("diff_time")
              // 如果第一次进来赋值初始值
              if (browseTime == null && browseTime!=0) {
                // 如果初次就 达到了预期值 则直接进行 触发器的设置
                if (diffTime >= ruleEvent.rule.timeInterval) {
                  registerTimeTimer(data, ruleEvent, ctx, 3)
                } else {
                  // 未达到则保存当前记录
                  cntMapState.put(ruleEvent.rule.ruleId.toString, diffTime)
                }
              } else {
                // 非首次进来
                if (browseTime + diffTime >= ruleEvent.rule.timeInterval) {
                  registerTimeTimer(data, ruleEvent, ctx, 3)
                } else {
                  // 保存
                  cntMapState.put(ruleEvent.rule.ruleId.toString, browseTime + diffTime)
                }
              }
            }

          }
        }
      }
    } catch {
      case e: Exception => {
        logger.error("error msg :"+ e.getMessage +"错误数据:" + ruleEvent)
      }
    }
  }


  /**
   * 注册触发器
   * @param ruleEvent 事件
   * @param ctx       上下文
   */
  def registerTimeTimer(data: JSONObject, ruleEvent: RuleEvent, ctx: KeyedBroadcastProcessFunction[String, RuleEvent, Rule, String]#ReadOnlyContext, pushType: Int): Unit = {
    // 用户最后触发事件 时间
    val time: Long = data.getLong("time").longValue()
    val value: String = new Gson().toJson(ruleEvent)
    // 注册时间戳      事件时间 + B事件时间间隔 (A事件默认为0) + 用户触发频率时间 + 用户延迟触发时间
    var timerTime: Long = time + ruleEvent.rule.timeInterval + ruleEvent.rule.cacheTime + ruleEvent.rule.delayedTrigger

    // 如果为页面浏览事件注册需要将 timeInterval减去   浏览时长 基于触发时进行注册时间
    // 如果不去掉的话 可能会导致 数据下发的延迟时间为  (浏览时间 + 事件时间 + 延迟触达)
    if (pushType == 3) {
      timerTime -= ruleEvent.rule.timeInterval
    }
    // 更新timeVaule 事件时间 + 触发频率时间 + 延迟触发时间
    eventValueState.update(timerTime)
    // 组装数据更新
    valueState.update(value)

    // 注册触发器 1. 表示已触发过 2.避免重复发送
    // 用户 触发频率触发器
//    println("注册触发频率触发器定时器：" + new Date(timerTime * 1000L))
    ctx.timerService().registerProcessingTimeTimer(timerTime * 1000)
    // 用户 延迟触发触发器
    timerTime -= ruleEvent.rule.cacheTime
//    println("注册延迟触发触发器定时器：" + new Date(timerTime * 1000L))
    ctx.timerService().registerProcessingTimeTimer(timerTime * 1000)
  }


  /**
   * 检查时间是否满足
   * @param ruleEvent  规则体
   * @param expression 表达式
   * @return 返回是否满足
   */
  def checkPatternData(ruleEvent: RuleEvent, expression: Expression, data: JSONObject): Boolean = {
    var cntFlag: Boolean = false
    // 判断A是否完成
    if (eventValueState.value() == null || eventValueState.value() == -1 || ruleEvent.rule.eventType == 2) {
      // 未完成 判断其表达式
      var params: JSONObject = new JSONObject()
      try {
        params = data.getJSONObject("params")
//        params.put("ps", params.getJSONObject("ps"))
      } catch {
        case e: Exception => {}
      }
      data.put("params", params)
      if (expression != null) {
//        AviatorEvaluator.execute()
        val flag: Boolean = expression.execute(data).toString.toBoolean
        // 如果满足 且次数达成 则将注册触发器
        if (flag) {
          val ruleCnt: Int = ruleEvent.rule.cnt
          // 如果次数为1 则进行触发器注册  注册时间为 频率（多久看一次）
          if (ruleCnt == 1) {
            cntFlag = true
          } else {
            // 拿取对应MapState 然后次数+1
            var cnt: Int = cntMapState.get(ruleEvent.rule.ruleId.toString) + 1

            // 如果满足则进行注册
            if (cnt == ruleCnt) {
              cnt = 0
              cntFlag = true
            }
            cntMapState.put(ruleEvent.rule.ruleId.toString, cnt)
          }
        }
      }
    }
    cntFlag
  }


  /**
   * 广播流处理新增规则
   *
   * @param rule 新增规则
   * @param ctx  上下文环境
   * @param out  输出
   */
  override def processBroadcastElement(rule: Rule, ctx: KeyedBroadcastProcessFunction[String, RuleEvent, Rule, String]#Context, out: Collector[String]): Unit = {

    val expressionMapState: BroadcastState[String, String] = ctx.getBroadcastState(Pojos.expressionMapDesc)
    logger.info("expression rule process " + rule)
    // 对规则进行新增删除处理
    rule.status match {
      case Constants.IFTTT_RULE.RUNNING | Constants.IFTTT_RULE.UPDATE =>
        logger.info("新增规则:" + new Gson().toJson(rule))
        try {

          // 规则库更新
          expressionMapState.put(rule.ruleId.toString, rule.patternStr)
        } catch {
          case e: Exception=>{
            logger.error("新增规则error, msg: " + e.getMessage + ", rule: " +  new Gson().toJson(rule))
            e.printStackTrace()
          }
        }



      case Constants.IFTTT_RULE.STOP =>
        logger.info("删除规则:" + new Gson().toJson(rule))
        expressionMapState.remove(rule.ruleId.toString)
    }
  }


}
