package com.hhz.ifttt.functions

import com.alibaba.fastjson.JSON
import com.hhz.ifttt.pojo.Constants.IFTTT_LOG
import com.hhz.ifttt.pojo.Pojos.IftttUserInfo
import com.hhz.ifttt.pojo.ResLog
import com.hhz.ifttt.utils.CommonUtil
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}

/**
 * @program ifttt 
 * @description: 资源位点击数据
 * @author: zyh 
 * @create: 2021/09/15 16:00 
 **/
class ResClickUserInfoFillFunction extends ProcessFunction[ResLog, IftttUserInfo]{
  private val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  override def processElement(res: ResLog, ctx: ProcessFunction[ResLog, IftttUserInfo]#Context, out: Collector[IftttUserInfo]): Unit = {
    try{
      val uid: String = res.getUid.toString
      val objId: String = res.getObjId.toString
      val objType: String = res.getObjType.toString
      val event: String = res.getModule.toString
      val time: Long = res.getTimestr.toString.toLong

      var authorId: String = ""
      if (StringUtils.isNotBlank(objId)) {
        authorId = CommonUtil.getAuthorId(objId)
      }
      var params: String = res.getDatas.toString
      if (StringUtils.isBlank(params)) {
        params = res.getParams.toString
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
      out.collect(IftttUserInfo(IFTTT_LOG.LOG_RESCLICK, uid, time, event, objId, objType, authorId, -1, params))

    }catch {
      case e: Exception => {
        logger.error("parse fiald, message:" + e.getMessage + ", value: " + res.toString)
        e.printStackTrace()
      }
    }
  }
}
