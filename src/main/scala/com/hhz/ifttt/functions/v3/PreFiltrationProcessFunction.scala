package com.hhz.ifttt.functions.v3


import com.hhz.ifttt.pojo.Constants.IFTTT_RULE
import com.hhz.ifttt.pojo.{Constants, Pojos}
import com.hhz.ifttt.pojo.Pojos.{OutLog, Rule, RuleEvent, ruleDesc}
import org.apache.flink.api.common.state.BroadcastState
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction
import org.slf4j.{Logger, LoggerFactory}
import java.lang
import java.util.Map
import java.util.function.Consumer

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.flink.api.scala.metrics.ScalaGauge
import org.apache.flink.util.Collector

import scala.collection.mutable

/**
 * @program ifttt 
 * @description: 预过滤
 * @author: zhangyinghao 
 * @create: 2022/10/18 19:25 
 **/
class PreFiltrationProcessFunction extends BroadcastProcessFunction[String, Rule, RuleEvent] with CheckpointedFunction {


  private val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  // rule 对应事件实体
  private var ruleEventMap: mutable.HashMap[String, mutable.HashSet[Rule]] = _

  // 规则库使用
  override def processElement(info: String, context: BroadcastProcessFunction[String, Rule, RuleEvent]#ReadOnlyContext, collector: Collector[RuleEvent]): Unit = {
    // 获取原模型数据
    //    val iftttUser: IftttUserInfo = JSON.parseObject(info, classOf[IftttUserInfo])
    //    if (info.equals("kill")) throw new Exception()
    if (!info.contains("8427009")) return
    try {
      var json: JSONObject = JSON.parseObject(info)
      val event: String = json.getString("event")
      val uid: String = json.getString("uid")
      try {
        val rules: mutable.HashSet[Rule] = ruleEventMap.getOrElse(event, null)
        if (rules != null && rules.nonEmpty) {
          // 能够获取到 说明都是被规则需要的
          rules.foreach(rule => {
            json.put("rule", rule)
            collector.collect(RuleEvent(uid, json.toJSONString, rule, uid))
          })
        } else {
          // ifttt_out_log
          context.output(Pojos.iftttOutLog, OutLog(uid, event, info, Constants.IFTTT_OUT_REASON.PRE_FILTER, System.currentTimeMillis() / 1000))
        }
      } catch {
        case e: Exception => {
          context.output(Pojos.iftttOutLog, OutLog(uid, event, info, String.format(Constants.IFTTT_OUT_REASON.RULE_MATCH_ERROR, e.getMessage), System.currentTimeMillis() / 1000))
        }
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }


  }


  /*------------------------------------------------------ 规则流处理 -------------------------------------------------------------------------------*/

  /**
   * 规则流Hash
   *
   * @param rule
   * @param context
   * @param collector
   */
  override def processBroadcastElement(rule: Rule, context: BroadcastProcessFunction[String, Rule, RuleEvent]#Context, collector: Collector[RuleEvent]): Unit = {
    logger.info("rule filter process : " + rule.toString)
    val ruleState: BroadcastState[Int, Rule] = context.getBroadcastState(Pojos.ruleDesc)
    handlerRuleEvent(rule, ruleState)
  }

  /**
   * 处理rule事件
   *
   * @param rule      rule规则实体
   * @param ruleState 规则状态
   */
  def handlerRuleEvent(rule: Pojos.Rule, ruleState: BroadcastState[Int, Pojos.Rule]) = {
    rule.status match {
      // 运行 | 新增
      case IFTTT_RULE.RUNNING =>
        // 更新state & 本地实体变量
        removeByRuleId(rule.ruleId, ruleState)
        addRule(rule, ruleState)

      // 更新
      case IFTTT_RULE.UPDATE =>
        // 先判断是否存在， 如果存在，先删除再更新 // 同event 情况
        removeByRuleId(rule.ruleId, ruleState)
        // 不存在
        addRule(rule, ruleState)

      // 停止，下线
      case IFTTT_RULE.STOP =>
        logger.info("删除rule:" + rule)
        removeByRuleId(rule.ruleId, ruleState)
        ruleState.remove(rule.ruleId)
      case _ =>
    }
  }

  def removeByRuleId(id: Int, ruleState: BroadcastState[Int, Pojos.Rule]): Unit = {
    ruleEventMap.foreach(ts => {
      val rules: mutable.HashSet[Rule] = ts._2.filter(t => t.ruleId != id)
      ruleEventMap.put(ts._1, rules)
    })
    ruleState.remove(id)
  }

  // 删除并返回新的
  def removeRule(rule: Rule): mutable.HashSet[Rule] = {
    val rules: mutable.HashSet[Rule] = ruleEventMap.getOrElse(rule.event, new mutable.HashSet[Rule])
    val newRuleSet: mutable.HashSet[Rule] = new mutable.HashSet[Rule]()

    rules.foreach(r => if (r.ruleId != rule.ruleId) newRuleSet.add(r))
    newRuleSet
  }

  // 新增Rule
  def addRule(rule: Pojos.Rule, ruleState: BroadcastState[Int, Pojos.Rule]): Unit = {
    val ruleSet: mutable.HashSet[Rule] = ruleEventMap.getOrElse(rule.event, new mutable.HashSet[Rule]())
    ruleSet.add(rule)
    ruleEventMap.put(rule.event, ruleSet)
    logger.info("新增rule: {}", rule.toString)
    ruleState.put(rule.ruleId, rule)
  }

  /*---------------------------------------  维度填充 --------------------------------------------------------*/


  /*------------------------------------ 配置初始化 -------------------------------------------*/


  @transient private var filterRate: ScalaGauge[Long] = _

  override def open(parameters: Configuration): Unit = {
    println("open")
    if (ruleEventMap == null) {
      ruleEventMap = new mutable.HashMap[String, mutable.HashSet[Rule]]()
    }
  }

  override def initializeState(context: FunctionInitializationContext): Unit = {
    logger.info("initializeState：" + context.isRestored)
    // 初始化state
    if (context.isRestored) {
      ruleEventMap = new mutable.HashMap[String, mutable.HashSet[Rule]]()
      logger.info("checkpoint恢复：")
      val iterator: lang.Iterable[Map.Entry[Int, Rule]] = context.getOperatorStateStore.getBroadcastState(ruleDesc).entries()
      iterator.forEach(new Consumer[Map.Entry[Int, Rule]] {
        override def accept(t: Map.Entry[Int, Rule]): Unit = {
          val rule: Rule = t.getValue
          logger.info("checkpoint恢复 广播：" + rule)
          val ruleSet: mutable.HashSet[Rule] = ruleEventMap.getOrElse(rule.event, new mutable.HashSet[Rule]())
          ruleSet.add(rule)
          ruleEventMap.put(rule.event, ruleSet)
        }
      })
    }
  }


  override def snapshotState(context: FunctionSnapshotContext): Unit = {
    logger.info("当前rule:" + ruleEventMap)
  }


}
