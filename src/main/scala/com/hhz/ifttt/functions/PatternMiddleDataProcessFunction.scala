package com.hhz.ifttt.functions

import java.text.SimpleDateFormat
import java.util.Date

import com.alibaba.fastjson.{JSON, JSONObject}
import com.hhz.ifttt.pojo.Pojos.PatternMiddleResult
import com.hhz.ifttt.utils.TimeUtil
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector

/**
 * @program ifttt 
 * @description: 中间匹配数据处理
 * @author: zhangyinghao 
 * @create: 2021/11/01 16:26 
 **/
@Deprecated
class PatternMiddleDataProcessFunction extends ProcessFunction[String, PatternMiddleResult] {
  val df: SimpleDateFormat = new SimpleDateFormat("yyyyMMdd")

  override def processElement(value: String, ctx: ProcessFunction[String, PatternMiddleResult]#Context, out: Collector[PatternMiddleResult]): Unit = {
    try {
      val ruleEvent: JSONObject = JSON.parseObject(value)
      if (ruleEvent != null) {
        val data: JSONObject = ruleEvent.getJSONObject("data")
        val rule: JSONObject = ruleEvent.getJSONObject("rule")
        var uid: Long = -1
        val pt: Long = System.currentTimeMillis()/1000
        try {
          uid = Option(ruleEvent.getString("uid")).getOrElse("-1").toLong
        } catch {
          case ex: java.lang.NumberFormatException => uid = -1
        }

        val time: Long = Option(data.getString("time")).getOrElse("-1").toLong
        val ruleId: Int = Option(rule.getString("ruleId")).getOrElse("-1").toInt
        val groupId: Int = Option(rule.getString("groupId")).getOrElse("-1").toInt
        val key: String = ruleEvent.getString("key")
        val logType: String = "pattern_middle"
        val hashCode: Int = value.hashCode()
        var day: Int = TimeUtil.timestampToDate(time, "yyyyMMdd").toInt
        if (!TimeUtil.checkDay(df, day)) {
          day = df.format(new Date()).toInt
        }
        out.collect(PatternMiddleResult(day, hashCode, logType, uid, pt, time,
          ruleId, groupId, key, value))
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }


  }
}

