package com.hhz.ifttt.functions

import com.alibaba.fastjson.JSONObject
import com.google.gson.Gson
import com.hhz.ifttt.pojo.Constants.IFTTT_LOG
import com.hhz.ifttt.pojo.Pojos.{ErrorData, IftttUserInfo, PvLog}
import com.hhz.ifttt.pojo.Constants
import org.apache.flink.api.scala._
import com.hhz.ifttt.utils.{CommonUtil, TimeUtil}
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}

/**
 * @program ifttt 
 * @description: pv数据数据清洗
 * @author: zyh 
 * @create: 2021/09/14 15:56 
 **/
class PvUserInfoFillFunction extends ProcessFunction[PvLog, IftttUserInfo]{

  private val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  override def processElement(pv: PvLog, ctx: ProcessFunction[PvLog, IftttUserInfo]#Context, out: Collector[IftttUserInfo]): Unit = {
    try{
      val objId: String = pv.oid.toString
      val time: Long = pv.report_time
      val event: String = pv.pagename.toString
      val uid: String = pv.uid.toString
      var authorId: String = ""
      // 如果 类型为浏览个人主页则 authorId = oid
      if ("user".equals(event)) {
          authorId = objId
      } else {
        if (StringUtils.isNotBlank(objId)) {
          authorId = CommonUtil.getAuthorId(objId)
        }
      }
      val json: JSONObject = new JSONObject()
      json.put("oid", objId)
      out.collect(IftttUserInfo(IFTTT_LOG.LOG_PV, uid, time, event, objId, "", authorId, -1, json.toString))
    }catch {
      case e: Exception => {
        logger.error("parse fiald, message:" + e.getMessage + ", value: " + pv.toString)
        e.printStackTrace()
      }
    }
  }
}
