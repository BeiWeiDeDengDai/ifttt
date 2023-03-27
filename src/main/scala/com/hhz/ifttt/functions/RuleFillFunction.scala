package com.hhz.ifttt.functions

import java.lang

import com.alibaba.fastjson.{JSON, JSONObject}
import com.googlecode.aviator.{AviatorEvaluator, Expression}
import com.hhz.ifttt.pojo.Pojos.Rule
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}
import org.apache.flink.streaming.api.scala._


/**
 * @program ifttt 
 * @description: 规则流填充
 * @author: zyh 
 * @create: 2021/09/17 16:10 
 **/
class RuleFillFunction extends ProcessFunction[String, Rule]{

  private val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)


  override def processElement(value: String, ctx: ProcessFunction[String, Rule]#Context, out: Collector[Rule]): Unit = {
    try {
      logger.info("规则: {}",value)
      val rule: JSONObject = JSON.parseObject(value)
      val ruleId: Integer = rule.getInteger("ruleId")
      val groupId: Integer = rule.getInteger("groupId")
      val dynamicFieldName: String = Option(rule.getString("dynamicFieldName")).getOrElse("")
      val status: String = Option(rule.getString("status")).getOrElse("")
      val event: String = Option(rule.getString("event")).getOrElse("")
      val dataSource: String = Option(rule.getString("dataSource")).getOrElse("")
      val dynamicDimensionField: String = Option(rule.getString("dynamicDimensionField")).getOrElse("")
      val isDimensionFill: lang.Boolean = Option(rule.getBoolean("isDimensionFill")).getOrElse(false)
      val patternStr: String = Option(rule.getString("patternStr")).getOrElse("")

      val cnt: Integer = Option(rule.getInteger("cnt")).getOrElse(1)
      val module: Integer = Option(rule.getInteger("module")).getOrElse(1)
      val time: String = Option(rule.getString("time")).getOrElse("")
      val isDynamicTime: lang.Boolean = Option(rule.getBoolean("isDynamicTime")).getOrElse(false)
      val eventType: Integer = Option(rule.getInteger("eventType")).getOrElse(1)
      val timeInterval: lang.Long = Option(rule.getLong("timeInterval")).getOrElse(0)
      val delayedTrigger: lang.Long = Option(rule.getLong("delayedTrigger")).getOrElse(0)
      val cacheTime: lang.Long = Option(rule.getLong("cacheTime")).getOrElse(0)
//      val cacheTime: lang.Long = 0.toLong
      val isUserProfile: lang.Boolean = Option(rule.getBoolean("isUserProfile")).getOrElse(false)
      val pushType: Integer = Option(rule.getInteger("pushType")).getOrElse(1)
      val bsId: String = Option(rule.getString("bsId")).getOrElse("")
      val userDimfield: Array[String] = Option(rule.getString("userDimField")).getOrElse("").split(",")
      out.collect(Rule(ruleId, groupId, dynamicFieldName,status, event, dataSource, dynamicDimensionField,
        isDimensionFill,cnt, patternStr, module, time, isDynamicTime, eventType, timeInterval, delayedTrigger
      ,cacheTime, isUserProfile, pushType, bsId,userDimfield))

    } catch {
      case e: Exception => {
        logger.error("解析ruleStream错误, msg:" + e.getMessage + ", value:" + value)
        e.printStackTrace()
      }
    }
  }
}
