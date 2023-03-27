package com.hhz.ifttt.pojo

import java.util
import org.apache.flink.api.scala._
import org.apache.flink.api.common.state.MapStateDescriptor
import org.apache.flink.streaming.api.scala.OutputTag
object Pojos {
  case class LoginEvent( userId: Long, ip: String, eventType: String, eventTime: Long,info:String )
  case class Warning( userId: Long, firstFailTime: Long, lastFailTime: Long, warningMsg: String)
  case class UA(uid:String,time:String,status:String,info:String)
  case class PatternResult(task: String,uid: String, pt: String, event_path: util.HashMap[String, String], params: String)

  case class Sensors(act_form: String, app_version: String, push_id: String, vid: String, uid: String, event_type: String, obj_type: String, tag: String
                     ,part_id: String, keyword: String, day: Int, obj_id: String, os: String, banner_id: String, ip: String, search_type: String, last_source: String,
                     time: Long, page: String, category: String, act_params: String, did: String, properties: String, part_name: String)


  /**
   * ifttt 基础数据合并实体
   * @param dataSource 日志类型
   * @param uid
   * @param time 时间戳
   * @param event 动作
   * @param oid 内容id
   * @parma objType 内容类型
   * @param authorId 作者id
   * @param authorUserType 被用户身份
   * @param params 额外参数信息
   */
  case class IftttUserInfo(dataSource: String, uid: String, time: Long, event: String, oid: String, objType: String, authorId: String, authorUserType: Int, params: String)



  /**
   * 错误信息
   * @param logType 日志类型
   * @param time 时间
   * @param errorMsg 错误信息
   * @param value 源数据
   */
  case class ErrorData(logType: String, time: Long, errorMsg: String, value: String)

  /**
   * 用户访问路径
   * @param uid
   * @param showId 单次访问id
   * @param objId 内容id
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @param pageName 页面
   * @param time 时间
   */
  case class UserAccessPath(uid: String, showId: String, objId: String, startTime: String, endTime: String, pageName: String, time: String)


  /**
   * 规则体
   * @param ruleId 规则id
   * @param groupId 任务id
   * @param dynamicFieldName 动态key字段  author, content
   * @param status 规则状态 1: success, 2: update, 3: remove
   * @param event 动作
   * @param dataSource 日志类型 sensors, action, resclick, resshow, pv, nginx
   * @param dynamicDimensionField 动态维度字段 主维度 author, brand, content
   * @param isDimensionFill 是否进行维度补全
   * @param cnt 时间次数
   * @param patternStr 规则串
   * @param module 1. 触发实际。 2。 受众用户
   * @param time 受众 用户时间范围 多少天 多天   如果是静态 则为字符串拼接。20210103-20210301
   * @param isDynamicTime 静态、动态时间
   * @param eventType: 事件类型 1:A, 2:B, 3:C
   * @param timeInterval AB间隔 / 浏览时长   仅当事件为B，C进行传递
   * @param delayedTrigger 延迟触发
   * @param cacheTime 缓存时间-- 触达  多久触发一次
   * @param isUserProfile 是否使用用户画像  侧输出流  未使用直接 输出 (flink_cep_pattern_result, 否则 dwd_ifttt_user_profile_data
   * @param pushType 触发类型： 1 完成A， 2 完成B ， 3 完成C
   *  @param bsId 业务维度id, 指明如何获取id值， 0: uid+作者id
   *                                        1: uid+内容id
   *                                        2: 作者id + uid
   *                                        3: uid
   *                                        4. 作者id
   *                                        5. 内容id
   */
  case class Rule(ruleId: Int, groupId: Int, dynamicFieldName: String, status: String,
                  event: String, dataSource: String, dynamicDimensionField: String,
                  isDimensionFill: Boolean, cnt: Int, patternStr: String, module: Int,
                  time: String, isDynamicTime: Boolean, eventType: Int, timeInterval: Long,
                  delayedTrigger: Long, cacheTime: Long, isUserProfile: Boolean, pushType: Int, bsId: String = "",
                  userDimField:Array[String]=Array(),
                  contentDimField:Array[String]=Array(),
                  brandDimField:Array[String]=Array(),
                  bsDimField:Array[String]=Array()
                 )
  /**
   * @param uid uid
   * @param data 原数据
   * @param rule 规则id
   */
  case class RuleEvent(uid: String,data: String,  rule: Rule, key: String)


  /**
   * 中间层数据
   * @param day 日期
   * @param hashcode hashcode
   * @param log_type 日志类型 1: middle, 2: userProfile
   * @param uid uid
   * @param pt 匹配时间
   * @param time 时间时间
   * @param rule_id ruleId
   * @param group_id 任务id
   * @param key key
   * @param data 原数据
   */
  case class PatternMiddleResult(day: Int, hashcode: Long, log_type: String, uid: Long, pt: Long, time: Long, rule_id: Int, group_id: Int, key: String,
                                data: String)

  /**
   * 用户任务数据
   * @param ruleId 任务id 等价于 groupId
   * @param uid uid
   * @param time 时间
   * @param data 原数据
   */
  case class UserTask(ruleId: String, groupId: String ,uid: String, time: Long, data: String)

  // 规则描述器
  val ruleDesc: MapStateDescriptor[Int, Rule] = new MapStateDescriptor[Int, Rule]("rule", classOf[Int], classOf[Rule])

  // 规则表达式描述器
  val expressionMapDesc: MapStateDescriptor[String, String] = new MapStateDescriptor[String, String]("expressionMapState", classOf[String], classOf[String])



  // *********************** --log-v2

  case class SensorsLog(offset: String, md5:String,uid:String,app_version:String,os:String,os_version:String,did:String,ip:String,network_type:String,report_time:Long,process_time:Long,browse_environment:String,model:String,session_id:String,release_mode:Int,`type`:String,day:Int,str:String,data_from:String,category:String,obj_id:String,obj_type:String,act_params:String,last_source:String,tag:String,keyword:String,banner_id:String,search_type:String,latest_referrer:String,time:Long,part_id:String,part_name:String,event_type:String,act_from:String,pagename:String,pos:String,stat_sign:String,show_id:String,params:String,app_version_code: String,c_id: String)

  case class ResShowLog(offset: String, md5:String,uid:String,app_version:String,os:String,os_version:String,did:String,ip:String,network_type:String,report_time:Long,process_time:Long,browse_environment:String,model:String,session_id:String,release_mode:Int,`type`:String,day:Int,time:String,app:String,datas:String,module_type:String,module:String,index:String,style:String,display_source:String,page:String,config_id:String,config_group:String,obj_uid:String,obj_id:String,obj_type:String,url_type:String,url:String,reason_type:String,reason:String,from_source:String,params:String,key_word:String,str:String,relate_obj_id:String,relate_obj_type:String,show_id:String,activity_name:String,pos:String,label:String,app_version_code: String,c_id: String)

  case class PvLog(offset: String,md5:String,uid:String,app_version:String,os:String,os_version:String,did:String,ip:String,network_type:String,report_time:Long,process_time:Long,browse_environment:String,model:String,session_id:String,release_mode:Int,`type`:String,day:Int,stat_sign:String,show_id:String,from:String,oid:String,url:String,pagename:String,start_time:Long,end_time:Long,cid:String,c_name:String,app_version_code: String, params: String,from_c_id: String)


  // **************** --ifttt-v3
  // 表描述器
  val tableProdesc: MapStateDescriptor[Int, TablePro] = new MapStateDescriptor[Int, TablePro]("rule", classOf[Int], classOf[TablePro])

  // 维度表配置
  case class TablePro(id: Int,cn: String, en: String, tables: String, key: String, patternStr: String,  status: String, storeType: Int, value: String)
  // 丢弃记录
  case class OutLog(uid: String, event: String, data: String, msg: String, time: Long)

  val iftttOutLog: OutputTag[OutLog] = OutputTag[OutLog]("ifttt_out_log")

  /*
     *
     * @param before
     * @param after
     * @param source
     * @param op
     * @param ts_ms
     * @param transaction
     */
  case class FlinkCdcModule(before: String, after: String, source: String, op: String, ts_ms: Long, transaction: String)

  case class FlinkCdcSourceModule(version: String, connector: String, name: String, ts_ms: String,snapshot: String, db: String, sequence: String, table: String, server_id: String,
                                  gtid: String, file: String, pos: String, row: String, thread: String, query: String)


  case class ActionLog(uid: String, time: Long, ip: String, url: String, `type`: String, params: String, user_agent: String, did: String, author_uid: String, day: Int, source: String
                       , beat_hostname: String)


}
