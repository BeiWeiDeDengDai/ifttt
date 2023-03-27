package com.hhz.ifttt.functions

import com.hhz.ifttt.pojo.{ActionLog, Pojos}
import com.hhz.ifttt.pojo.Constants.IFTTT_LOG
import com.hhz.ifttt.pojo.Pojos.{ErrorData, IftttUserInfo}
import com.hhz.ifttt.utils.{CommonUtil, TimeUtil}
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}
/**
 * @program ifttt 
 * @description: 行为日志数据清洗
 * @author: zyh 
 * @create: 2021/09/14 10:19 
 **/
class ActionUserInfoFillFunction extends ProcessFunction[Pojos.ActionLog, IftttUserInfo]{


  private val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  override def processElement(action: Pojos.ActionLog, ctx: ProcessFunction[Pojos.ActionLog, IftttUserInfo]#Context, out: Collector[IftttUserInfo]): Unit = {
    try{
      val uid: String = action.uid.toString
      var params: String = action.params.toString
      val time: Long = TimeUtil.parseDateToTimestamp(action.time.toString, "yyyyMMddHHmmss") / 1000
      val event: String = action.`type`.toString
//
      var objId: String = ""
      var authorId: String = ""
      // 获取 objId
      if (StringUtils.isNotBlank(params)) {
        objId = CommonUtil.getObjId(params)

        if (StringUtils.isNotBlank(objId)) {
          // 获取作者id
          authorId = CommonUtil.getAuthorId(objId)
        }
      }
      params = CommonUtil.getJSON(params)
      out.collect(IftttUserInfo(IFTTT_LOG.LOG_ACTION,uid, time, event, objId, "", authorId, -1, params))
    }catch {
      case e: Exception => {
        logger.error("parse fiald, message:" + e.getMessage + ", value: " + action.toString)
        e.printStackTrace()
      }
    }
  }
}
