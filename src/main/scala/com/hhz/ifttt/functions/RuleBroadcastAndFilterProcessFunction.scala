package com.hhz.ifttt.functions

import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.{lang, util}

import avro.shaded.com.google.common.cache.{Cache, CacheBuilder}
import com.alibaba.fastjson.{JSON, JSONObject}
import com.google.gson.Gson
import com.googlecode.aviator.Expression
import com.hhz.ifttt.pojo.Constants.IFTTT_COMMON_PATTER_PROCESS
import com.hhz.ifttt.pojo.Pojos.{Rule, RuleEvent}
import com.hhz.ifttt.pojo.{Constants, Pojos}
import com.hhz.ifttt.utils.RedisUtil
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.state.{BroadcastState, ListState, ListStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}
import redis.clients.jedis.Jedis

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * @program ifttt 
 * @description: 规则流广播以及规则预过滤
 * @author: zyh 
 * @create: 2021/09/16 16:51 
 **/
class RuleBroadcastAndFilterProcessFunction extends BroadcastProcessFunction[String, Rule, RuleEvent] with CheckpointedFunction {


  private val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  // 规则体
  var ruleEvents: ListState[(String, Integer)] = _

  //  redis 连接配置
    val redisConfInfo: (String, Int, String) = RedisUtil.getRedis()
//  val redisConfInfo: (String, Int, String) =
//  val redisConfInfo: (String, Int, String) = ("r-2zeyxh2w62nxpq7q0h.redis.rds.aliyuncs.com", 6379, "hZrdS15zong")
//RedisUtil.getRedisHostAndPort(Constants.REDIS_PATH, Constants.REDIS_FLAG)

  var jedis: Jedis = _


  // baseUserTypeCache 用户身份cache
  var baseUserTypeCache: Cache[String, String] = CacheBuilder.newBuilder()
    //设置并发级别为8，并发级别是指可以同时写缓存的线程数
    .concurrencyLevel(3)
    //设置缓存容器的初始容量为10
    .initialCapacity(4 * 1000)
    //设置缓存最大容量为100，超过100之后就会按照LRU最近虽少使用算法来移除缓存项
    .maximumSize(5 * 1000)
    //设置如果后多久未访问就将其清除
    .expireAfterAccess(2, TimeUnit.DAYS)
    .build()

  // baseContentTypeCache 内容类型cache
  var baseContentTypeCache: Cache[String, String] = CacheBuilder.newBuilder()
    .concurrencyLevel(3)
    .initialCapacity(4 * 10000)
    .maximumSize(5 * 10000)
    .expireAfterAccess(1, TimeUnit.DAYS)
    .build()

  // dynamicDimCache 动态维度cache
  var dynamicDimCache: Cache[String, String] = CacheBuilder.newBuilder()
    .concurrencyLevel(3)
    .initialCapacity(4 * 10000)
    .maximumSize(5 * 10000)
    .expireAfterAccess(1, TimeUnit.DAYS)
    .build()


  var expressionMap: mutable.Map[String, Expression] = _


  override def open(parameters: Configuration): Unit = {
    // Redis 连接初始化
    jedis = new Jedis(redisConfInfo._1, redisConfInfo._2)
    if (!redisConfInfo._1.equals("localhost")){
          jedis.auth(redisConfInfo._3)
    }

    logger.info("redis conf:" + redisConfInfo)
  }


  override def close(): Unit = {
    if (jedis != null) {
      jedis.close()
    }
  }


  /**
   * 处理 事件数据  对于State为 read 权限
   *
   * @param value 源数据
   * @param ctx
   * @param out
   */
  override def processElement(value: String, ctx: BroadcastProcessFunction[String, Rule, RuleEvent]#ReadOnlyContext, out: Collector[RuleEvent]): Unit = {
    try {
      // 获取当前最新的
      if (!value.contains("8427009")) return
      var data: JSONObject = JSON.parseObject(value)
      val event: String = data.getString("event")
      // 如果存在  可能存在一个事件对应多个规则
      val ruleIds: ArrayBuffer[Int] = isExists(ruleEvents.get(), event)
      if (ruleIds.nonEmpty) {
        for (ruleId <- ruleIds) {
          val rule: Rule = ctx.getBroadcastState(Pojos.ruleDesc).get(ruleId)
          if (rule != null) {
            // 维度填充
            data = dimFill(data, rule)

            // 动态key判断
            var key: String = data.getString("uid") + "," + rule.groupId
            if (StringUtils.isNotBlank(rule.dynamicFieldName)) {
              rule.dynamicFieldName.split(",").foreach(i => {
                if (i.equals(Constants.IFTTT_DIM.DIM_AUTHOR) || i.equals(Constants.IFTTT_DIM.DIM_BRAND)) {
                  key = key + "," + data.getString("authorId")
                } else if (i == Constants.IFTTT_DIM.DIM_CONTENT) {
                  key = key + "," + data.getString("oid")
                } else if (i == Constants.IFTTT_DIM.DIM_CONTENT_TYPE) {
                  key = key + "," + data.getString("objType")
                }
              })
            }
            out.collect(RuleEvent(data.getString("uid"), data.toJSONString, rule, key))
          } else {
            logger.info("ruleId 可能已被删除 ruleId:" + ruleId + ", value: " + value)
          }
        }
      }
    } catch {
      case e: Exception => {
        logger.error("processElement faild:" + e.getMessage + "value: " + value)
        e.printStackTrace()
      }
    }
  }


  /**
   * 处理 广播流数据 对于State提供 read / warite 权限
   *
   * @param rule rule数据
   * @param ctx
   * @param out
   */
  override def processBroadcastElement(rule: Rule, ctx: BroadcastProcessFunction[String, Rule, RuleEvent]#Context, out: Collector[RuleEvent]): Unit = {
    logger.info("rule filter process : " + rule.toString)
    // 对Rule进行处理
    val ruleState: BroadcastState[Int, Rule] = ctx.getBroadcastState(Pojos.ruleDesc)
    handlerRule(rule, ruleState)


  }

  def addRule(rule: Rule): Unit = {
    deleteRule(rule.ruleId)
    val ruleLists: util.ArrayList[(String, Integer)] = new util.ArrayList[(String, Integer)]()

    ruleEvents.get().forEach(new Consumer[(String, Integer)] {
      override def accept(t: (String, Integer)): Unit = {
        if (!t._2.equals(rule.ruleId)) {
          ruleLists.add(t)
        }
      }
    })

    ruleLists.add((rule.event, rule.ruleId))
    ruleEvents.update(ruleLists)
  }

  def deleteRule(ruleId: Int): Unit = {
    val ruleLists: util.ArrayList[(String, Integer)] = new util.ArrayList[(String, Integer)]()
    // 遍历添加 只添加未被标记的event
    ruleEvents.get().forEach(new Consumer[(String, Integer)] {
      override def accept(t: (String, Integer)): Unit = {
        if (!t._2.equals(ruleId)) {
          ruleLists.add(t)
        }
      }
    })
    ruleEvents.update(ruleLists)
  }


  /**
   * 处理新到规则
   *
   * @param rule      规则
   * @param ruleState 历史规则
   */
  def handlerRule(rule: Rule, ruleState: BroadcastState[Int, Rule]): Unit = {
    try {
      rule.status match {
        case Constants.IFTTT_RULE.RUNNING | Constants.IFTTT_RULE.UPDATE =>
          logger.info("新增规则:" + new Gson().toJson(rule))
          addRule(rule)
          // 规则库更新
          ruleState.put(rule.ruleId, rule)


        case Constants.IFTTT_RULE.STOP =>
          logger.info("删除规则:" + rule.toString)
          deleteRule(rule.ruleId)
          ruleState.remove(rule.ruleId)
      }
    } catch {
      case e: Exception => {

      }
    }

  }


  /**
   * 判断是否存在
   * todo 匹配效率有问题
   *    1. 可以改为 hashMap<String, List<Integer>>   evnet, RuleIds 结构  同步在规则流的时候 也需要做改动 （）
   *
   * @param rules 规则集合
   * @param event 事件
   * @return
   */
  def isExists(rules: lang.Iterable[(String, Integer)], event: String): ArrayBuffer[Int] = {
    val listBuffer: ArrayBuffer[Int] = new ArrayBuffer[Int]()
    val iterator: util.Iterator[(String, Integer)] = rules.iterator()
    // 遍历规则集合判断是否有其存在
    while (iterator.hasNext) {
      val e: (String, Integer) = iterator.next()
      if (e._1.equals(event)) listBuffer += e._2
    }
    listBuffer
  }


  /**
   * 维度填充
   * todo 维度数据准备   2redis airflow
   * DimData2Redis
   * DimDesignerData2Redis 动态维度补充
   *
   * @param data 源数据
   * @param rule 规则
   */
  def dimFill(data: JSONObject, rule: Rule): JSONObject = {
    if (rule.isDimensionFill) {
      // 维度补全-用户身份
      val authorId: String = data.getString("authorId")
      var userType: String = baseUserTypeCache.getIfPresent(authorId)
      if (StringUtils.isBlank(userType)) {
        userType = jedis.hget("user_type", authorId)
        // 如果获取不到则表名未普通用户
        if (userType == null) {
          userType = "0"
        } else {
          baseUserTypeCache.put(authorId, userType)
        }
      }
      data.put("authorUserType", userType)

      // 内容类型
      val objId: String = data.getString("oid")
      var objType: String = baseContentTypeCache.getIfPresent(objId)
      if (StringUtils.isEmpty(objType)) {
        objType = jedis.hget("obj_type", objId)
        if (objType == null) objType = "-1" else baseContentTypeCache.put(objId, objType)
      }
      data.put("objType", objType)


      // 动态维度补全
      if (StringUtils.isNotBlank(rule.dynamicDimensionField)) {
        val fields: Array[String] = rule.dynamicDimensionField.split(",")
        fields.foreach(field => {
          // 1.主维度 author, brand, content
          // 2.维度属性
          // 3.需求值 (作者id, 品牌id, 内容id)
          var itemId: String = ""
          val items: Array[String] = field.split(":")
          items(0) match {
            case Constants.IFTTT_DIM.DIM_AUTHOR | Constants.IFTTT_DIM.DIM_BRAND => itemId = authorId
            case Constants.IFTTT_DIM.DIM_CONTENT => itemId = objId
          }

          // 如果填充维度是品牌
          if (items(0).equals(Constants.IFTTT_DIM.DIM_BRAND) && items(1).equals("cate_name")) {
            val userActon: (String, String) = (data.getString("dataSource"), data.getString("event"))
            val cates: (String, String) = getCates(userActon, data, baseContentTypeCache, dynamicDimCache, jedis)
            val param: JSONObject = data.getJSONObject("params")
            param.put(items(1), cates._2)
            data.put("params", param)
          } else {
            // field+value拼接获取
            var itemValue: String = dynamicDimCache.getIfPresent(items(1) + itemId)
            if (StringUtils.isEmpty(itemValue)) {
              val key: String = String.format(Constants.IFTTT_DYNAMIC_DIM_FIELD_PREFIX, items(0), items(1))
              itemValue = jedis.hget(key, itemId)
              if (itemValue == null) itemValue = ""
              dynamicDimCache.put(items(1) + itemId, itemValue)
            }
            data.put(items(1), itemValue)
          }

        })
      }
    }
    data
  }


  def getCates(userAction: (String, String), value: JSONObject, noteIdCache: Cache[String, String], categoryCache: Cache[String, String], jedis: Jedis) = {

    var cates: (String, String) = ("", "")
    val params: String = value.getString("params")
    // 用户临时分数计算
    userAction match {

      // 搜索
      case ("action", "search") =>
        cates = (value.getString("oid"), value.getJSONObject("params").getString("tag"))

      // 浏览榜单
      case ("action", "wiki_ranking_hot") | ("action", "wiki_ranking_brand") | ("action", "wiki_ranking_publicpraise") =>
        cates = getCateNamesByWikiId(4, value, noteIdCache, categoryCache, jedis)

      // 浏览 wiki 内容
      case ("action", "biz_wikidetail_goodsinfo") =>
        // 根据wikiId获取品类id
        cates = getCateNamesByWikiId(1, value, noteIdCache, categoryCache, jedis)

      case ("pv", _) =>
        if (!"user".equals(userAction._2)) {
          cates = getCateNames(value.getString("obj_id"), noteIdCache, categoryCache, jedis)
        }

      // 收藏 相关wiki 商品
      case ("action", "biz_wiki_fav") =>
        cates = getCateNamesByWikiId(2, value, noteIdCache, categoryCache, jedis)

      // 去购买
      case ("sensors", "button_click") =>
        // 点击为 去购买
        if (StringUtils.isNotBlank(params)) {
          val json: JSONObject = JSON.parseObject(params)
          val actionType: String = json.getString("type")
          val objType: String = json.getString("obj_type")
          if (StringUtils.isNotBlank(actionType) && "buy_now".equals(actionType)) {
            if (StringUtils.isNotBlank(objType) && "goods".equals(objType)) {
              cates = getCateNamesByWikiId(3, value, noteIdCache, categoryCache, jedis)
            }
          }
        }

      // 交互
      case ("action", "like") | ("action", "favorite") | ("action", "comment") =>
        cates = getCateNames(value.getString("obj_id"), noteIdCache, categoryCache, jedis)

      case _ =>
    }

    cates

  }

  /**
   * 根据wikiId获取
   *
   * @param value         当前值
   * @param noteIdCache   note缓存
   * @param categoryCache category缓存
   * @param jedis         redis
   * @return
   */
  def getCateNamesByWikiId(id: Int, value: JSONObject, noteIdCache: Cache[String, String], categoryCache: Cache[String, String], jedis: Jedis): (String, String) = {

    var categoryResult: (String, String) = ("", "")

    try {
      val params: JSONObject = value.getJSONObject("params")
      // 针对不同动作去获取wikiId
      var wikiId: String = ""
      id match {
        // 商品详情
        case 1 => wikiId = params.getString("wikiID")
        // 收藏
        case 2 =>
          wikiId = IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_WIKI_BASIC + params.getString("wiki_id")
        // 去购买
        case 3 => wikiId = IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_WIKI_BASIC + params.getString("obj_id")
        // 浏览榜单
        case 4 => wikiId = IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_RANK_NOTE_CATEGORY + params.getString("ranking_id")
      }
      if (StringUtils.isNotBlank(wikiId)) {
        categoryResult = getCateNames(wikiId, noteIdCache, categoryCache, jedis)
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }
    categoryResult
  }

  /**
   * 根据 obj_id / wikiId  获取品牌名称
   *
   * @param objId         内容id / wikiId
   * @param noteIdCache   内容id 缓存
   * @param categoryCache category 缓存
   * @param jedis         redis
   * @return （内容categoryId, 内容categoryName）
   */
  def getCateNames(objId: String, noteIdCache: Cache[String, String], categoryCache: Cache[String, String], jedis: Jedis): (String, String) = {
    var cateResuls: String = ""
    var contentIds: String = ""
    try {
      // 根据内容id 获取其对应的categoryId
      contentIds = noteIdCache.getIfPresent(objId)

      // 如果拿取不到则从redis中获取
      if (!StringUtils.isNotBlank(contentIds)) {
        // 如果是榜单类 则从榜单缓存中获取
        if (objId.contains(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_RANK_NOTE_CATEGORY)) {
          contentIds = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_RANK_NOTE_CATEGORY, objId.replaceAll(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_RANK_NOTE_CATEGORY, ""))
        } else {

          // 否则先从note_category获取
          contentIds = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_NOTE_CATEGORY, objId)
          // 如果note_category为空说明不是内容id 则从wikiId中获取
          if (!StringUtils.isNotBlank(contentIds)) {
            var tmpWikiId: String = ""
            if (objId.contains(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_WIKI_BASIC)) { // 如果是收藏 需要使用wikiId找到对应的basicId
              tmpWikiId = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_WIKI_BASIC, objId.replaceAll(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_WIKI_BASIC, ""))
            }
            if (StringUtils.isNotBlank(tmpWikiId)) {
              contentIds = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_WIKI_CATEGORY, tmpWikiId)
            } else {
              contentIds = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_WIKI_CATEGORY, objId)
            }

          }
        }
      }

      // 如果是多品类则要多次发送, 这里需要拿取对应的categoryName， 从品类兴趣库 category_interest 中拿取。
      if (StringUtils.isNotBlank(contentIds)) {
        // 将已获取到的contentIds 进行缓存
        noteIdCache.put(objId, contentIds)

        // 获取实际的cateName
        val buffer: StringBuffer = new StringBuffer()
        contentIds.split(",").foreach(cateId => {
          var cateName: String = categoryCache.getIfPresent(cateId)
          if (StringUtils.isBlank(cateName)) {
            cateName = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_CATEGORY, cateId)
            if (StringUtils.isNotBlank(cateName)) {
              categoryCache.put(cateId, cateName)
            }
          }
          if (cateName == null) cateName = ""
          buffer.append(cateName.trim)
            .append(",")
        })

        val result: String = buffer.toString
        cateResuls = result.substring(0, result.length - 1)
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }
    (contentIds, cateResuls)
  }


  /**
   * 初始化
   *
   * @param context
   */
  override def initializeState(context: FunctionInitializationContext): Unit = {
    ruleEvents = context.getOperatorStateStore.getListState(new ListStateDescriptor[(String, Integer)]("ruleEvents", classOf[(String, Integer)]))
  }

  override def snapshotState(context: FunctionSnapshotContext): Unit = {
    val ruleLists: util.ArrayList[(String, Integer)] = new util.ArrayList[(String, Integer)]()
    // 遍历添加 只添加未被标记的event
    ruleEvents.get().forEach(new Consumer[(String, Integer)] {
      override def accept(t: (String, Integer)): Unit = {
        ruleLists.add(t)
      }
    })
    ruleEvents.update(ruleLists)
  }
}
