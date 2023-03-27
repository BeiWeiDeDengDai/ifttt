package com.hhz.ifttt.functions

import com.alibaba.fastjson.JSON
import com.hhz.ifttt.pojo.Constants.{IFTTT_LOG, LOG_TYPE}
import com.hhz.ifttt.pojo.Pojos.{IftttUserInfo, ResShowLog}
import com.hhz.ifttt.utils.CommonUtil
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}

/**
 * @program ifttt 
 * @description: 资源位曝光数据
 * @author: zyh 
 * @create: 2021/09/15 16:00 
 **/
class ResShowUserInfoFillFunction extends ProcessFunction[ResShowLog, IftttUserInfo]{
  private val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  override def processElement(res: ResShowLog, ctx: ProcessFunction[ResShowLog, IftttUserInfo]#Context, out: Collector[IftttUserInfo]): Unit = {
    try{
      val uid: String = res.uid.toString
      val objId: String = res.obj_id.toString
      val objType: String = res.obj_type.toString
      val event: String = res.module.toString
      val time: Long = res.time.toLong

      var authorId: String = ""
      if (StringUtils.isNotBlank(objId)) {
        authorId = CommonUtil.getAuthorId(objId)
      }

      var params: String = res.datas.toString
      if (StringUtils.isBlank(params)) {
        params = res.params.toString
      } else {
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
      }
      var tp: String = IFTTT_LOG.LOG_RESSHOW
      res.`type` match {
        case LOG_TYPE.LOG_RESSHOW=> tp = IFTTT_LOG.LOG_RESSHOW
        case LOG_TYPE.LOG_RESCLICK => tp = IFTTT_LOG.LOG_RESCLICK
        case _=>
      }

      out.collect(IftttUserInfo(tp, uid, time, event, objId, objType, authorId, -1, params))

    }catch {
      case e: Exception => {
        logger.error("parse fiald, message:" + e.getMessage + ", value: " + res.toString)
        e.printStackTrace()
      }
    }
  }
}
