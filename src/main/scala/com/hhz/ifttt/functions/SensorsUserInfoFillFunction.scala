package com.hhz.ifttt.functions

import java.lang

import com.alibaba.fastjson.JSON
import com.google.gson.Gson
import com.hhz.ifttt.pojo.Constants.IFTTT_LOG
import com.hhz.ifttt.pojo.Pojos.{ErrorData, IftttUserInfo, SensorsLog}
import com.hhz.ifttt.pojo.Constants
import com.hhz.ifttt.utils.CommonUtil
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.scala.OutputTag
import org.slf4j.{Logger, LoggerFactory}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector

/**
 * @program ifttt 
 * @description:
 * @author: zyh 
 * @create: 2021/09/13 16:17 
 **/
class SensorsUserInfoFillFunction extends ProcessFunction[SensorsLog, IftttUserInfo] {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  override def processElement(sensors: SensorsLog, ctx: ProcessFunction[SensorsLog, IftttUserInfo]#Context, out: Collector[IftttUserInfo]): Unit = {
    try {

      val uid: String = sensors.uid.toString
      val event: String = sensors.category.toString
      val time: lang.Long = sensors.time
      val objId: String = sensors.obj_id.toString
      val objType: String = sensors.obj_type.toString
      var authorId: String = ""
      if (StringUtils.isNotBlank(objId)) {
        authorId = CommonUtil.getAuthorId(objId)
      }
      var params: String = sensors.act_params.toString
      if (StringUtils.isNotBlank(params)) {
        // 可能不是json格式
        // tag=装修避坑 , &uid=871576
        // ideabook=2443236&{"a":"app","mt":"ideabook","m":"ideabook-ideabook_content_card","ps":[],"s":"list","ds":"primal","oi":"003p5bq00005ynyi","roi":2443236}

        params = params.replace("statSign=", "")
        val ps: Array[String] = params.split("\\u0026").filter(x => {
          var flag: Boolean = false
          try {
            if (JSON.parseObject(x) != null) {
              flag = true
            }
          }catch {
            case e: Exception =>
              flag = false
          }
          flag
        })
        if (ps.length > 0) {
          params = ps(0)
        }

      } else {
        params = sensors.params.toString
      }

      // 如果都为空 将line当做params
      if (StringUtils.isBlank(params)) {
        params = sensors.str.toString
      }

      out.collect(IftttUserInfo(IFTTT_LOG.LOG_SENSORS,uid, time, event, objId, objType, authorId, -1, params))
    } catch {
      case e: Exception => {
        logger.error("parse fiald, message:" + e.getMessage + ", value: " + sensors.toString)
        e.printStackTrace()
      }
    }
  }
}
