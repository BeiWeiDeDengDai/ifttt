package com.hhz.ifttt.functions

import com.hhz.ifttt.pojo.NginxLog
import com.hhz.ifttt.pojo.Pojos.UserAccessPath
import com.hhz.ifttt.utils.CommonUtil
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}

/**
 * nginx数据清洗
 */
class UserAccessExtractProcessFunction extends ProcessFunction[NginxLog, UserAccessPath] {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)
    override def processElement(nginx: NginxLog, ctx: ProcessFunction[NginxLog, UserAccessPath]#Context, out: Collector[UserAccessPath]): Unit = {
      try {
        if (nginx.getUrl.toString.startsWith("/detail_page_duration")) {
          // 用户访问
          val params: Map[String, String] = CommonUtil.paramsToMap(nginx.getParams.toString)
          val showId: String = params.getOrElse("show_id", "")
          val startTime: String = params.getOrElse("start_time", "")
          val endTime: String = params.getOrElse("end_time", "")
          val objId: String = params.getOrElse("obj_id", "")
          val pageName: String = params.getOrElse("page_name", "")
          // 如果objId不为空
          if (StringUtils.isNotBlank(objId)) {
            // 根据 showId 分组
            out.collect(UserAccessPath(nginx.getUid.toString, showId, objId, startTime, endTime, pageName, nginx.getTime.toString))
          }
        }
      } catch {
        case e: Exception => {
          logger.error("parse fiald, message:" + e.getMessage + ", value: " + nginx.toString)
          e.printStackTrace()
        }
      }
      // 用户访问换个人主页
    }
  }