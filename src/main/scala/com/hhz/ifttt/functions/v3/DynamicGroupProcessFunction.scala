package com.hhz.ifttt.functions.v3

import com.alibaba.fastjson.{JSON, JSONObject}
import com.hhz.ifttt.pojo.Pojos
import com.hhz.ifttt.pojo.Pojos.{OutLog, RuleEvent}
import org.apache.commons.lang3.{StringEscapeUtils, StringUtils}
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector

/**
 * @program ifttt 
 * @description: 对事件进行筛选，动态key
 * @author: zhangyinghao 
 * @create: 2023/01/09 14:52 
 **/
class DynamicGroupProcessFunction extends ProcessFunction[String, (String,RuleEvent)] {


  override def processElement(value: String, ctx: ProcessFunction[String, (String,RuleEvent)]#Context, out: Collector[(String,RuleEvent)]): Unit = {
    val ruleEvent: RuleEvent = JSON.parseObject(value, classOf[RuleEvent])

    if (ruleEvent.rule.pushType == 1) {
      try{
        // 用户id + 任务id + 规则id
        var key: String = ruleEvent.uid + "_" + ruleEvent.rule.groupId + "_" + ruleEvent.rule.ruleId
        val data: JSONObject = JSON.parseObject(ruleEvent.data)
        val dynamicFieldName: String = ruleEvent.rule.dynamicFieldName
        if (StringUtils.isNotBlank(dynamicFieldName)) {
          // 动态key的选择--- 为了后续可扩展性
          var dynamicFn: String = null
          dynamicFieldName match {
            // 同一作者
            case "author_id" =>
              dynamicFn =  data.getString("author_id")
            // 同一内容
            case "oid" =>
              dynamicFn =  data.getString("obj_id")
            case _ =>
              // 动态选择根据 dynamicFieldName 从数据流中获取
              dynamicFn = data.getString(dynamicFieldName)
          }

          if (StringUtils.isNotBlank(dynamicFn)) {
            key = key + "_" + dynamicFn
          }
        }
        out.collect((key, ruleEvent))
      }catch {
        case exception: Exception =>
          ctx.output(Pojos.iftttOutLog, OutLog("", "", StringEscapeUtils.unescapeJson(value), s"无法正常解析: ${exception.getMessage}", System.currentTimeMillis()))
      }

    }
  }


}
