package com.hhz.ifttt.functions.v3

import java.util.concurrent.{CompletableFuture, TimeUnit}
import java.util.function.{Consumer, Supplier}

import com.alibaba.fastjson.{JSON, JSONObject}
import com.hhz.ifttt.pojo.Constants
import com.hhz.ifttt.pojo.Constants.IFTTT_COMMON_PATTER_PROCESS
import com.hhz.ifttt.pojo.Pojos.{Rule, RuleEvent}
import com.hhz.ifttt.utils.RedisUtil
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.{RedisClient, RedisURI}
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.scala.metrics.ScalaGauge
import org.apache.flink.configuration.Configuration
import org.apache.flink.metrics.Counter
import org.apache.flink.shaded.guava30.com.google.common.cache.{Cache, CacheBuilder}
import org.apache.flink.streaming.api.scala.async.{ResultFuture, RichAsyncFunction}

/**
 * @program ifttt
 * @description: Redis 异步请求
 * @author: zhangyinghao
 * @create: 2022/12/28 16:34
 **/
class AsyncRedisRequestFunction extends RichAsyncFunction[RuleEvent, RuleEvent] {

  // 预过滤数


  val redisConfInfo: (String, Int, String) =
  //    ("r-2zeyxh2w62nxpq7q0h.redis.rds.aliyuncs.com", 6379, "hZrdS15zong")
    RedisUtil
      .getRedis()
  //      .getRedisHostAndPort(Constants.REDIS_PATH, Constants.REDIS_FLAG)

  var redisClient: RedisClient = _
  private var async: RedisAsyncCommands[String, String] = _
  //    .getRedisHostAndPort(Constants.REDIS_PATH, Constants.REDIS_FLAG)
  // 缓存加速
  private var cache: Cache[String, String] = _
  // 指标匹配数
  @transient private var patternCounter: Counter = _
  // 维度匹配数-redis
  @transient private var dynamicDimRedisCounter: Counter = _
  // 本地缓存命中数
  @transient private var localCacheCounter: Counter = _

  @transient private var durtime: Counter = _


  @transient private var patternQps: ScalaGauge[Long] = _

  override def open(parameters: Configuration): Unit = {

    //    jedis = new Jedis(redisConfInfo._1, redisConfInfo._2, 100000)
    //    println(jedis.ping())
    //    jedis.auth(redisConfInfo._3)
    val redisUri: RedisURI = RedisURI.builder()
      .withHost(redisConfInfo._1)
      //      .withPassword(redisConfInfo._3)
      .withPort(redisConfInfo._2).build()
    import java.time.Duration

    redisClient = RedisClient.create(redisUri)


    println(s"redis://${redisConfInfo._3}@${redisConfInfo._1}:${redisConfInfo._2}")
    async = redisClient.connect().async()


    // 初始化缓存
    if (cache == null) {
      cache = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES) // 5分钟内没有更新
        .maximumSize(40 * 10000)
        .initialCapacity(20 * 10000)
        .build()
    }
    patternCounter = getRuntimeContext.getMetricGroup.counter("patternCounter")
    dynamicDimRedisCounter = getRuntimeContext.getMetricGroup.counter("dynamicDimRedisCounter")
    durtime = getRuntimeContext.getMetricGroup.counter("durtime")
    localCacheCounter = getRuntimeContext.getMetricGroup.counter("localCacheCounter")

    patternQps = getRuntimeContext.getMetricGroup.gauge[Long, ScalaGauge[Long]]("patternQps", ScalaGauge[Long](() => patternCounter.getCount * 1000 / durtime.getCount))

  }

  override def timeout(input: RuleEvent, resultFuture: ResultFuture[RuleEvent]): Unit = {
    println("timeout" + input.rule)
  }


  override def asyncInvoke(input: RuleEvent, resultFuture: ResultFuture[RuleEvent]): Unit = {
    // 需要进行维度填充
    if (input.rule.isDimensionFill) {
      // 维度数据补充
      CompletableFuture.supplyAsync(new Supplier[RuleEvent] {
        override def get(): RuleEvent = {
          // todo 维度匹配
          val startTime: Long = System.currentTimeMillis()
          patternCounter.inc(1)
          val json: JSONObject = dimFill(input.rule, JSON.parseObject(input.data), async)
          input.copy(data = json.toJSONString)
          durtime.inc(System.currentTimeMillis()-startTime)
          input
        }
      }).thenAccept(new Consumer[RuleEvent] {
        override def accept(t: RuleEvent): Unit = {
          resultFuture.complete(Iterable(t))
        }
      })
    } else {
      resultFuture.complete(Iterable(input))
    }
  }


  /**
   * 动态维度填充
   */
  def dimFill(rule: Rule, info: JSONObject, jedis: RedisAsyncCommands[String, String]): JSONObject = {
    val dimFields: String = rule.dynamicDimensionField
    // 用户动态维度属性："user:nick, content: obj_type，admin_score"
    dimFields.split(",").foreach(field => {
      val items: Array[String] = field.split(":")
      // 主维度
      val mainDim: String = items(0)
      var rowKey: String = ""
      // 主维度k
      var mainKey: String = ""
      //      var infoKey = ""
      // 特殊维度 商品
      if (mainDim.equals("brand") && items(1).equals("cate_name")) {
        val userActon: (String, String) = (info.getString("dataSource"), info.getString("event"))
        val cates: (String, String) = getCates(userActon, info, cache, cache, jedis)
        if (StringUtils.isNoneBlank(cates._2)) {
          //          val param: JSONObject = info.getJSONObject("params")
          info.put(mainDim + "_" + items(1), cates._2)
        }
      } else {
        mainDim match {
          case "user" =>
            // 主要匹配  用户类型
            mainKey = "user"
            rowKey = info.getString("authorId")
          case "content" =>
            mainKey = "content"
            rowKey = info.getString("oid")
          case _ =>
            mainKey = "bs"
            val k: String = "%s:%s"
            // 业务维度id
            //          @param bsId 业务维度id, 指明如何获取id值，0: uid + 作者id
            //      * 0: uid + 内容id
            //      * 1: 作者id + uid
            //      * 2: uid
            //      * 3. 作者id
            //      * 4.内容id   xxxx 自定义获取id  todo 需要定义与维护  维度规则中 key 与 rule，实体类中字段对应关系
            //                              例如： 维度规则 key: photo_id    与 实体类中字段   oid 对应 那么对应的枚举值应该为 下面的4
            rowKey = rule.bsId match {
              case "0" => String.format(k, info.getString("uid"), info.getString("authorId"))
              case "1" => String.format(k, info.getString(
                "authorId"), info.getString("uid"))
              case "2" => info.getString("uid")
              case "3" => info.getString("authorId")
              case "4" => info.getString("oid")
              case _ => info.getString("uid")
            }
        }

        // 匹配业务维度数据
        items.slice(1, items.length).foreach(field => {
          // 找出业务维度key值， 进行填充
          // dim_ifttt_user_type  dim_ifttt_content_circle
          val key: String = String.format(Constants.IFTTT_DYNAMIC_DIM_FIELD_PREFIX_V3, mainKey, field)
          try {
            val value: String = getCacheValue(key, rowKey)
            if (StringUtils.isNoneBlank(value)) {
              info.put(mainDim + "_" + field, value)
            }
          } catch {
            case e: Exception =>
              e.printStackTrace()
          }
        })
      }
    })
    info
  }


  /**
   * // 从缓存中获取值
   *
   * @param key    维度key
   * @param rowKey 主键rowKey
   * @return
   */
  def getCacheValue(key: String, rowKey: String): String = {
    var value: String = null
    val queryKey: String = key + "_" + rowKey
    try {
      var value: String = cache.getIfPresent(queryKey)
      if (value == null) {
        value = async.get(queryKey).get(4, TimeUnit.SECONDS)
        if (StringUtils.isNotBlank(value)) {
          cache.put(key + "_" + rowKey, value);
        }
      }

    } catch {
      case exception: Exception => {
        println(s"匹配异常：" + exception.getMessage)
      }
    }
    value
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
  def getCateNames(objId: String, noteIdCache: Cache[String, String], categoryCache: Cache[String, String], jedis: RedisAsyncCommands[String, String]): (String, String) = {
    var cateResuls: String = ""
    var contentIds: String = ""
    try {
      // 根据内容id 获取其对应的categoryId
      contentIds = noteIdCache.getIfPresent(objId)

      // 如果拿取不到则从redis中获取
      if (!StringUtils.isNotBlank(contentIds)) {

        // 如果是榜单类 则从榜单缓存中获取
        if (objId.contains(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_RANK_NOTE_CATEGORY)) {
          contentIds = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_RANK_NOTE_CATEGORY, objId.replaceAll(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_RANK_NOTE_CATEGORY, "")).get()
        } else {

          // 否则先从note_category获取
          contentIds = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_NOTE_CATEGORY, objId).get()
          // 如果note_category为空说明不是内容id 则从wikiId中获取
          if (!StringUtils.isNotBlank(contentIds)) {
            var tmpWikiId: String = ""
            if (objId.contains(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_WIKI_BASIC)) { // 如果是收藏 需要使用wikiId找到对应的basicId
              tmpWikiId = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_WIKI_BASIC, objId.replaceAll(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_WIKI_BASIC, "")).get()
            }
            if (StringUtils.isNotBlank(tmpWikiId)) {
              contentIds = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_WIKI_CATEGORY, tmpWikiId).get()
            } else {
              contentIds = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_WIKI_CATEGORY, objId).get()
            }
          }
        }
        //        if (StringUtils.isNotBlank(contentIds)) {
        //          dynamicDimRedisCounter.inc(1)
        //        }
      }
      //      else {
      //        localCacheCounter.inc(1)
      //      }

      // 如果是多品类则要多次发送, 这里需要拿取对应的categoryName， 从品类兴趣库 category_interest 中拿取。
      if (StringUtils.isNotBlank(contentIds)) {
        // 将已获取到的contentIds 进行缓存
        noteIdCache.put(objId, contentIds)

        // 获取实际的cateName
        val buffer: StringBuffer = new StringBuffer()
        contentIds.split(",").foreach(cateId => {
          var cateName: String = categoryCache.getIfPresent(cateId)
          if (StringUtils.isBlank(cateName)) {
            cateName = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_CATEGORY, cateId).get()
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


  def getCates(userAction: (String, String), value: JSONObject, noteIdCache: Cache[String, String], categoryCache: Cache[String, String], jedis: RedisAsyncCommands[String, String]) = {

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
        cates = getCateNames(value.getString("oid"), noteIdCache, categoryCache, jedis)

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
  def getCateNamesByWikiId(id: Int, value: JSONObject, noteIdCache: Cache[String, String], categoryCache: Cache[String, String], jedis: RedisAsyncCommands[String, String]): (String, String) = {

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

}
