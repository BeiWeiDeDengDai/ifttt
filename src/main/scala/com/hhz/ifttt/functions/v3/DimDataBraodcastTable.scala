package com.hhz.ifttt.functions.v3


import java.{lang, util}
import java.util.Map
import java.util.function.Consumer

import com.alibaba.fastjson.{JSON, JSONObject}
import com.google.common.base.Strings
import com.googlecode.aviator.{AviatorEvaluator, Expression}
import com.hhz.ifttt.pattern.v3.Operation
import com.hhz.ifttt.pojo.Constants.IFTTT_RULE
import com.hhz.ifttt.pojo.Pojos
import com.hhz.ifttt.pojo.Pojos.{FlinkCdcModule, Rule, TablePro, ruleDesc}
import com.hhz.ifttt.utils.RedisUtil
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.state.BroadcastState
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}
import redis.clients.jedis.Jedis

import scala.collection.mutable

/**
 * @program ifttt 
 * @description: ${TODO}
 * @author: zhangyinghao 
 * @create: 2022/10/26 13:20 
 **/
class DimDataBraodcastTable extends BroadcastProcessFunction[String, String, String] with CheckpointedFunction {


  private val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)
  private var tableProMap: mutable.HashMap[String, mutable.HashSet[TablePro]] = _ // 可能多个规则使用这个表

  val redisConf: (String, Int, String) = RedisUtil.getRedis();
  var jedis: Jedis = _



  override def processElement(value: String, ctx: BroadcastProcessFunction[String, String, String]#ReadOnlyContext, out: Collector[String]): Unit = {
    try {
      // 落表
      if (!value.contains("8427009")) return
      val cdcModule: FlinkCdcModule = JSON.parseObject(value, classOf[FlinkCdcModule])
      val cdcSource: JSONObject = JSON.parseObject(Option(cdcModule.source).getOrElse(""))

      val table: String = Option(cdcSource.getString("table")).getOrElse("")
      val database: String = Option(cdcSource.getString("db")).getOrElse("")

      if (StringUtils.isNotBlank(table) && StringUtils.isNotBlank(database)) {
        // 获取相关表配置
        val tablePros: mutable.HashSet[TablePro] = tableProMap.getOrElse(database + "." + table, null)
        if (tablePros != null) {
          // 对表进行过滤操作
          tablePros.foreach(tbPro => {
            val data: JSONObject = JSON.parseObject(cdcModule.after)
            if (data != null) {
              val sbr: StringBuilder = new StringBuilder
              tbPro.key.split(",").foreach(k => sbr.append(data.getString(k)).append(":"))
              if (sbr.length < 1) return
              val key: String = sbr.substring(0, sbr.length - 1)

              cdcModule.op match {
                // 如果是删除事件
                case Operation.DELETE =>
                  removeKey(jedis, tbPro.en, key, tbPro.storeType)
                case Operation.READ | Operation.CREATE | Operation.UPDATE =>
                  // 如果满足 则塞入对应
                  // {"cn":"是否加入圈子","en":"isJoinCircle","tables":"circle.join_circle","key":"uid,circle_id","patternStr":"join_status=1","status":"running"}
                  var result: Boolean = true
                  // 如果不为空 说明需要进行过滤
                  if (StringUtils.isNotBlank(tbPro.patternStr)) {
                    val ex: Expression = AviatorEvaluator.compile(tbPro.patternStr, true)
                    // 拿取数据进行匹配
                    result = ex.execute(data).toString.toBoolean
                  }
                  if (result) {
                    addKey(jedis, tbPro.en, key, tbPro.storeType, tbPro.value, data)
                  } else {
                    // 说明不满足了，干掉
                    removeKey(jedis, tbPro.en, key, tbPro.storeType)
                  }
              }
            }
          })
        }
      }
    } catch {
      case e: Exception =>
        logger.error("处理任务失败：" + value)
        e.printStackTrace()
    }
  }

  /**
   * 添加key
   *
   * @param jedis
   * @param key
   * @param field
   * @param storeType
   */
  def addKey(jedis: Jedis, key: String, field: String, storeType: Int,value: String, data: JSONObject): Unit = {
    var va = ""
    storeType match {
      case 0 =>
        va = data.getString(value)
      case 1 =>
        va = "1"
      case _=>
    }
    if (storeType == 2) {
      val icount: String = jedis.hget(key, field)
      if (StringUtils.isNotBlank(icount)) {
        jedis.hset(key, field, (icount.toInt + 1).toString)
      } else {
        jedis.hset(key, field, "1")
      }
    } else {
      // 对于匹配成功的数据，我们放入进去 key: en, field: uid_circle_id, value 1
      jedis.hset(key, field, va)
    }
  }

  /** *
   * 移除key
   *
   * @param jedis
   * @param key
   * @param field
   * @param storeType
   */
  def removeKey(jedis: Jedis, key: String, field: String, storeType: Int): Unit = {
    if (storeType == 2) {
      val icount: String = jedis.hget(key, field)
      if (StringUtils.isNotBlank(icount)) {
        if (icount.toInt > 1) {
          jedis.hset(key, field, (icount.toInt - 1).toString)
        }
      }
    } else {
      jedis.hdel(key, field)
    }
  }


  /**
   * 处理表信息
   *
   * @param value
   * @param ctx
   * @param out
   */
  override def processBroadcastElement(value: String, ctx: BroadcastProcessFunction[String, String, String]#Context, out: Collector[String]): Unit = {
    try {
      val tablePro: TablePro = JSON.parseObject(value, classOf[TablePro])
      if (tablePro == null) return
      val proState: BroadcastState[Int, TablePro] = ctx.getBroadcastState(Pojos.tableProdesc)
      handleTablePro(tablePro, proState)
    } catch {
      case e: Exception =>
        logger.error("规则处理错误")
        e.printStackTrace()
    }

  }


  // 删除并返回新的
  def removeRule(rule: TablePro): mutable.HashSet[TablePro] = {
    val rules = tableProMap.getOrElse(rule.tables, new mutable.HashSet[TablePro]())
    val newRuleSet: mutable.HashSet[TablePro] = new mutable.HashSet[TablePro]()
    rules.foreach(r => if (r.id != rule.id) newRuleSet.add(r))
    newRuleSet
  }

  // 新增Rule
  def addRule(rule: TablePro, ruleState: BroadcastState[Int, TablePro]): Unit = {
    val ruleSet: mutable.HashSet[TablePro] = tableProMap.getOrElse(rule.tables, new mutable.HashSet[TablePro]())
    ruleSet.add(rule)
    tableProMap.put(rule.tables, ruleSet)
    logger.info("新增rule: {}", rule.toString)
    ruleState.put(rule.id, rule)
  }

  /**
   * 处理规则信息
   *
   * @param tablePro
   * @param proState
   */
  def handleTablePro(tablePro: TablePro, proState: BroadcastState[Int, TablePro]) = {
    tablePro.status match {
      // 运行 | 新增
      case IFTTT_RULE.RUNNING =>
        // 更新state & 本地实体变量
        addRule(tablePro, proState)

      // 更新
      case IFTTT_RULE.UPDATE =>
        // 先判断是否存在， 如果存在，先删除再更新
        if (tableProMap.contains(tablePro.tables)) {
          val newRuleSet: mutable.HashSet[TablePro] = removeRule(tablePro)
          newRuleSet.add(tablePro)
          tableProMap.put(tablePro.tables, newRuleSet)
          logger.info("更新rule:" + tablePro)
          proState.put(tablePro.id, tablePro)
        } else {
          // 不存在
          addRule(tablePro, proState)
        }

      // 停止，下线
      case IFTTT_RULE.STOP =>
        if (tableProMap.contains(tablePro.tables)) {
          val newRuleSet: mutable.HashSet[TablePro] = removeRule(tablePro)
          tableProMap.put(tablePro.tables, newRuleSet)
          logger.info("删除rule:" + tablePro)
          proState.remove(tablePro.id)
        }
      case _ => logger.info("无效规则")
    }
  }

  override def open(parameters: Configuration): Unit = {
    println("open")
    if (tableProMap == null) {
      tableProMap = new mutable.HashMap[String, mutable.HashSet[TablePro]]()
    }
    jedis = new Jedis(redisConf._1, redisConf._2)
    if (!redisConf._1.equals("localhost")) {
      jedis.auth(redisConf._3)
    }
  }

  override def initializeState(context: FunctionInitializationContext): Unit = {
    logger.info("initializeState：" + context.isRestored)
    // 初始化state
    if (context.isRestored) {
      tableProMap = new mutable.HashMap[String, mutable.HashSet[TablePro]]()
      logger.info("checkpoint恢复：")
      val iterator: lang.Iterable[util.Map.Entry[Int, TablePro]] = context.getOperatorStateStore.getBroadcastState(Pojos.tableProdesc).entries()
      iterator.forEach(new Consumer[util.Map.Entry[Int, TablePro]] {
        override def accept(t: util.Map.Entry[Int, TablePro]): Unit = {
          val rule: TablePro = t.getValue
          logger.info("checkpoint恢复 广播：" + rule)
          val ruleSet: mutable.HashSet[TablePro] = tableProMap.getOrElse(rule.tables, new mutable.HashSet[TablePro]())
          ruleSet.add(rule)
          tableProMap.put(rule.tables, ruleSet)
        }
      })
    }
  }


  override def snapshotState(context: FunctionSnapshotContext): Unit = {
    logger.info("当前tablePro:" + tableProMap)
  }

}
