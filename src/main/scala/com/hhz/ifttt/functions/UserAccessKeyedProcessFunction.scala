package com.hhz.ifttt.functions

import com.alibaba.fastjson.JSONObject
import com.hhz.ifttt.pojo.Constants.IFTTT_LOG
import com.hhz.ifttt.pojo.Pojos.{IftttUserInfo, UserAccessPath}
import com.hhz.ifttt.utils.{CommonUtil, TimeUtil}
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.state.{StateTtlConfig, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.time.Time
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}


/**
 * nginx用户浏览页面时长清洗
 */
class UserAccessKeyedProcessFunction extends KeyedProcessFunction[(String, String), UserAccessPath, IftttUserInfo] {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)
  private var startTimeValueState: ValueState[Long] = _


  override def open(parameters: Configuration): Unit = {
    val startTimeValueDesc: ValueStateDescriptor[Long] = new ValueStateDescriptor[Long]("startTimeValue", classOf[Long])
    val ttlConfig: StateTtlConfig = StateTtlConfig
      .newBuilder(Time.days(1))
      .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite) // 仅在创建和写入时更新
      .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired) // 读取时也更新
      .build
    startTimeValueDesc.enableTimeToLive(ttlConfig)
    startTimeValueState = getRuntimeContext.getState(startTimeValueDesc)
  }

  override def processElement(access: UserAccessPath, ctx: KeyedProcessFunction[(String, String), UserAccessPath, IftttUserInfo]#Context, out: Collector[IftttUserInfo]): Unit = {
    try {
      // 浏览页面超过2秒以上
      if ((startTimeValueState.value() == null || startTimeValueState.value() == -1) && StringUtils.isNotBlank(access.startTime)) {
        // 更新
        startTimeValueState.update(access.startTime.toLong)
      } else {
        val startTime: Long = startTimeValueState.value()
        if (StringUtils.isNotBlank(access.endTime)) {
          val diffTime: Long = (access.endTime.toLong - startTime) / 1000
          var authorId: String = ""
          if (StringUtils.isNotBlank(access.objId)) {
            authorId = CommonUtil.getAuthorId(access.objId)
          }
          val json: JSONObject = new JSONObject()
          json.put("diff_time", diffTime)
          json.put("page_name", access.pageName)
          json.put("start_time", startTime/1000)
          json.put("end_time", access.endTime.toLong/1000)
          json.put("show_id", access.showId)
          startTimeValueState.clear()
          out.collect(IftttUserInfo(IFTTT_LOG.LOG_NGINX, access.uid, access.endTime.toLong / 1000, "nginx_access_page", access.objId, "", authorId, -1, json.toJSONString))
        }
      }
    } catch {
      case e: Exception => {
        logger.error("parse fiald, message:" + e.getMessage + ", value: " + access.toString)
        e.printStackTrace()
      }
    }
  }
}