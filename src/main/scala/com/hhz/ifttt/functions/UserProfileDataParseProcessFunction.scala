package com.hhz.ifttt.functions

import java.text.SimpleDateFormat
import java.{lang, util}
import java.util.Date

import com.alibaba.fastjson.{JSON, JSONObject}
import com.google.gson.Gson
import com.hhz.ifttt.pojo.Constants
import com.hhz.ifttt.pojo.Pojos.{PatternMiddleResult, PatternResult}
import com.hhz.ifttt.utils.TimeUtil
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector

/**
 * @program ifttt 
 * @description: 用户画像数据解析
 * @author: zhangyinghao 
 * @create: 2021/11/01 16:51 
 **/
@Deprecated
class UserProfileDataParseProcessFunction extends ProcessFunction[String, PatternMiddleResult] {
  val df: SimpleDateFormat = new SimpleDateFormat("yyyyMMdd")

  override def processElement(value: String, ctx: ProcessFunction[String, PatternMiddleResult]#Context, out: Collector[PatternMiddleResult]): Unit = {

    try {
      val userTask: JSONObject = JSON.parseObject(value)
      if (userTask != null) {
        val uid: Long = Option(userTask.getString("uid")).getOrElse("-1").toLong
        val ruleId: Int = Option(userTask.getString("ruleId")).getOrElse("-1").toInt
        val groupId: Int = Option(userTask.getString("groupId")).getOrElse("-1").toInt
        val time: Long = Option(userTask.getString("time")).getOrElse("-1").toLong
        var day: Int = TimeUtil.timestampToDate(time.toLong, "yyyyMMdd").toInt
        if (!TimeUtil.checkDay(df, day)) {
          day = df.format(new Date()).toInt
        }
        val logType: String = "userProfile"
        out.collect(PatternMiddleResult(day, hashCode, logType, uid, System.currentTimeMillis() / 1000, time,
          ruleId, groupId, "", value))
      }
    }
    catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }

  }

}
