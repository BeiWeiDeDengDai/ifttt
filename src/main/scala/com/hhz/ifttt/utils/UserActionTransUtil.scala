package com.hhz.ifttt.utils

import com.alibaba.fastjson.{JSON, JSONObject}
import com.hhz.ifttt.pattern.Commpattern.UserInfo
import com.hhz.ifttt.pojo.{ActionLog, PvLog, ResLog, SensorsLog, UserAction}
import org.apache.commons.lang3.{ObjectUtils, StringUtils}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration

/**
 * @program flinkcep 
 * @description: ${TODO} 
 * @author: zhangyinghao 
 * @create: 2020/10/19 10:07 
 **/
object UserActionTransUtil {
  case class Action(data_source: String, uid: String, time: Long, did: String, `type`: String, obj_id: String, url: String, params: String,
                    module_type: String, category: String, obj_type: String, part_name: String, last_source: String,
                    act_from: String, keyword: String, act_params: String, banner_id: String, search_type: String,
                    line: String, str: String, properties: String, module: String, posisign: String, id: String, datas: String,
                    device_type: String, config_id: String, url_type: String, display_source: String, page: String, index: String, reason_type: String,
                    reason: String, from_source: String, user_agent: String, push_id: String, author_id: String
                   )




  def getUserAction(userAction: Action): UserAction = {
    if (userAction == null) return null
    UserAction.newBuilder()
      .setDataSource(userAction.data_source)
      .setUid(userAction.uid)
      .setTime(userAction.time.toString)
      .setDid(userAction.did)
      .setType(userAction.`type`)
      .setObjId(userAction.obj_id)
      .setUrl(userAction.url)
      .setParams(userAction.params)
      .setModuleType(userAction.module_type)
      .setCategory(userAction.category)
      .setObjType(userAction.obj_type)
      .setPartName(userAction.part_name)
      .setLastSource(userAction.last_source)
      .setActFrom(userAction.act_from)
      .setKeyword(userAction.keyword)
      .setActParams(userAction.act_params)
      .setBannerId(userAction.banner_id)
      .setSearchType(userAction.search_type)
      .setLine(userAction.line)
      .setStr(userAction.str)
      .setProperties(userAction.properties)
      .setModule(userAction.module)
      .setPosisign(userAction.posisign)
      .setId(userAction.id)
      .setDatas(userAction.datas)
      .setDeviceType(userAction.device_type)
      .setConfigId(userAction.config_id)
      .setUrlType(userAction.url_type)
      .setDisplaySource(userAction.display_source)
      .setPage(userAction.page)
      .setIndex(userAction.index)
      .setReasonType(userAction.reason_type)
      .setReason(userAction.reason)
      .setFromSource(userAction.from_source)
      .setUserAgent(userAction.user_agent)
      .setPushId(userAction.push_id)
      .setAuthorId(userAction.author_id)
      .setUserType("")
      .build()
  }


  /**
   * 行为数据
   */
  class UserInfoMap extends RichMapFunction[UserInfo, UserAction] {
    var actionSourceLineNums: LongCounter = new LongCounter()
    var actionSinkLineNums: LongCounter = new LongCounter()

    override def open(parameters: Configuration): Unit = {
      getRuntimeContext.addAccumulator("actionSourceLineNums", actionSourceLineNums)
      getRuntimeContext.addAccumulator("actionSinkLineNums", actionSinkLineNums)
    }

    override def map(value: UserInfo): UserAction = {
      if (this.actionSourceLineNums.getLocalValue > 1000000000L) this.actionSourceLineNums.resetLocal()
      if (this.actionSinkLineNums.getLocalValue > 1000000000L) this.actionSinkLineNums.resetLocal()
      this.actionSourceLineNums.add(1)
      val uid: Option[String] = Option(value.uid.toString)
      val time: Option[String] = Option(value.time.toString)
      val did: Option[String] = Option("")
      val moudel_type: Option[String] = Option("")
      val eventType: Option[String] = Option(value.action.toString)
      var obj_id: Option[String] = Option("")
      try {
        obj_id = Option(value.oid.toString)
      }
      val url: Option[String] = Option("")
      val category: Option[String] = Option("")
      val obj_type: Option[String] = Option("")
      val partName: Option[String] = Option("")
      val lastSource: Option[String] = Option("")
      val actFrom: Option[String] = Option("")
      val keyword: Option[String] = Option("")
      val params: Option[String] = Option(getJSON(value.params.toString))
      val json = JSON.parseObject(params.get)
      json.put("cnt", value.cnt)
      json.put("taskId", value.taskId)
      json.put("ruleId", value.ruleId)
      json.put("category", value.category)
      json.put("userType", value.userType)
      val actParams: Option[String] = Option("")
      val bannerId: Option[String] = Option("")
      val searchType: Option[String] = Option("")
      val line: Option[String] = Option("")
      val str: Option[String] = Option("")
      val properties: Option[String] = Option("")
      val module: Option[String] = Option("")
      val posisign: Option[String] = Option("")
      val id: Option[String] = Option("")
      val datas: Option[String] = Option("")
      val config_id: Option[String] = Option("")
      val url_type: Option[String] = Option("")
      val display_source: Option[String] = Option("")
      val page: Option[String] = Option("")
      val index: Option[String] = Option("")
      val reason_type: Option[String] = Option("")
      val reason: Option[String] = Option("")
      val from_source: Option[String] = Option("")
      val device_type: Option[String] = Option("")
      val user_agent: Option[String] = Option("")
      val push_id: Option[String] = Option("")
      var author_id: Option[String] = Option("")
      if (StringUtils.isNotBlank(obj_id.getOrElse("")) && obj_id.getOrElse("").length == 16) {
        try {
          author_id = Option(ConvFunction.transRadix(obj_id.get.substring(10), 36, 10))
        } catch {
          case e: Exception => {
            println("解析失败" + obj_id.get)
            e.printStackTrace()
          }
        }
      }

      var action: Action = null
      try {
        action = Action("Commpattern", uid.getOrElse(""), time.getOrElse("").toLong, did.getOrElse(""), eventType.getOrElse(""), obj_id.getOrElse(""), url.getOrElse(""),
          json.toString, moudel_type.getOrElse(""), category.getOrElse(""), obj_type.getOrElse(""), partName.getOrElse(""), lastSource.getOrElse(""), actFrom.getOrElse(""), keyword.getOrElse(""),
          actParams.getOrElse(""), bannerId.getOrElse(""), searchType.getOrElse(""), line.getOrElse(""), str.getOrElse(""), properties.getOrElse(""), module.getOrElse(""), posisign.getOrElse(""),
          id.getOrElse(""), datas.getOrElse(""), config_id.getOrElse(""), url_type.getOrElse(""), display_source.getOrElse(""), page.getOrElse(""), index.getOrElse(""),
          reason_type.getOrElse(""), reason.getOrElse(""), from_source.getOrElse(""), device_type.getOrElse(""), user_agent.getOrElse(""), push_id.getOrElse(""), author_id.getOrElse(""))
      } catch {
        case e: Exception => {
          e.printStackTrace()
          println(value)
        }
      }
      if (action != null) {
        this.actionSinkLineNums.add(1)
      }

      getUserAction(action)
    }
  }


  /**
   * params 字段解析
   *
   * @param str
   * @return
   */
  def getJSON(str: String): String = {
    val nObject: JSONObject = new JSONObject()
    if (str != null) {
      str.split(",").map(info => {
        val items: Array[String] = info.split("=")
        try {
          nObject.put(items(0), items(1))
        } catch {
          case e: Exception => null
        }
      })
    }
    nObject.toString
  }

  /**
   * 获取objId
   * @param params
   * @return
   */
  def getObjId(params: String): String = {
    val paramsMap: Map[String, String] = strToMap(params)
    val objId: String = paramsMap.getOrElse("obj_id", null)
    val articleId: String = paramsMap.getOrElse("article_id", null)
    val activity_id: String = paramsMap.getOrElse("activity_id", null)
    val ideabook_id: String = paramsMap.getOrElse("ideabook_id", null)
    val photo_id: String = paramsMap.getOrElse("photo_id", null)
    val answer_id: String = paramsMap.getOrElse("answer_id", null)
    val question_id: String = paramsMap.getOrElse("question_id", null)
    val blank_id: String = paramsMap.getOrElse("blank_id", null)
    ObjectUtils.firstNonNull(objId, articleId, activity_id, ideabook_id, photo_id, answer_id, question_id, blank_id)
  }

  /**
   * 字符串转map
   *
   * @param str
   * @return
   */
  def strToMap(str: String): Map[String, String] = {
    val map: Map[String, String] = str.split(",").map(info => {
      val items: Array[String] = info.split("=")
      try {
        (items(0), items(1))
      } catch {
        case e: Exception => null
      }
    }).filter(_ != null).toMap
    map
  }
}
