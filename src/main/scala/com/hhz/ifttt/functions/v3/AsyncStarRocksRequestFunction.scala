package com.hhz.ifttt.functions.v3

import java.sql.{Connection, DriverManager, ResultSet, ResultSetMetaData, Statement}
import java.util.concurrent.{CompletableFuture, TimeUnit}
import java.util.function.{Consumer, Supplier}
import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}
import com.hhz.ifttt.pojo.Constants.{IFTTT_COMMON_PATTER_PROCESS, IFTTT_DIMM_FIELD}
import com.hhz.ifttt.pojo.Pojos.{Rule, RuleEvent}
import com.hhz.ifttt.utils.RedisUtil
import org.apache.commons.lang3.{StringEscapeUtils, StringUtils}
import org.apache.flink.api.scala.metrics.ScalaGauge
import org.apache.flink.configuration.Configuration
import org.apache.flink.metrics.Counter
import org.apache.flink.shaded.guava30.com.google.common.cache.{Cache, CacheBuilder}
import org.apache.flink.streaming.api.scala.async.{ResultFuture, RichAsyncFunction}
import redis.clients.jedis.Jedis

/**
 * @program ifttt
 * @description: Redis 异步请求
 * @author: zhangyinghao
 * @create: 2022/12/28 16:34
 **/
class AsyncStarRocksRequestFunction extends RichAsyncFunction[RuleEvent, RuleEvent] {

  // starrocks jdbc
  @transient var connection: Connection = _
  val redisConfInfo: (String, Int, String) =
  //    ("r-2zeyxh2w62nxpq7q0h.redis.rds.aliyuncs.com", 6379, "hZrdS15zong")
    RedisUtil
      .getRedis()
  @transient var jedis: Jedis = _
  // 缓存加速
  private var cache: Cache[String, String] = _
  @transient private var patternCounter: Counter = _
  // 维度匹配数-redis
  @transient private var sRCounter: Counter = _
  // 本地缓存命中数
  @transient private var localCacheCounter: Counter = _

  @transient private var durtime: Counter = _

  @transient private var patternQps: ScalaGauge[Long] = _

  override def open(parameters: Configuration): Unit = {

    // starRocks
    val url: String = "jdbc:mysql://172.17.0.32:9030,172.17.0.33:9030,172.17.0.34:9030/rt_ods"
    val user: String = "bigdata"
    val password: String = "zong2015"
    connection = DriverManager.getConnection(url, user, password)

    // redis
    jedis = new Jedis(redisConfInfo._1, redisConfInfo._2)
    if (!redisConfInfo._1.equals("localhost")) {
      jedis.auth(redisConfInfo._3)
    }

    // 初始化缓存
    if (cache == null) {
      cache = CacheBuilder.newBuilder()
        .expireAfterWrite(60, TimeUnit.SECONDS) // 5分钟内没有更新
        .maximumSize(40 * 10000)
        .initialCapacity(20 * 10000)
        .build()
    }


    patternCounter = getRuntimeContext.getMetricGroup.counter("patternCounter")
    sRCounter = getRuntimeContext.getMetricGroup.counter("srCounter")
    durtime = getRuntimeContext.getMetricGroup.counter("durtime")
    localCacheCounter = getRuntimeContext.getMetricGroup.counter("localCacheCounter")

    patternQps = getRuntimeContext.getMetricGroup.gauge[Long, ScalaGauge[Long]]("patternQps", ScalaGauge[Long](() => patternCounter.getCount * 1000 / durtime.getCount))

  }


  override def close(): Unit = {
    if (connection != null) {
      connection.close()
    }

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
          val startTime: Long = System.currentTimeMillis()
          patternCounter.inc(1)
          val json: JSONObject = dimFill(input.uid, input.rule, JSON.parseObject(input.data), connection)
          durtime.inc(System.currentTimeMillis() - startTime)
          input.copy(data = json.toJSONString)
//          input.copy(data = StringEscapeUtils.unescapeJson(json.toJSONString))
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
  def dimFill(uid: String, rule: Rule, info: JSONObject, connection: Connection): JSONObject = {


    // 首先冲缓冲拿取 通过 uid 与 ruleId 组合Key, 缺点: 如果rule发生改变 会导致旧数据
    // todo 检测到有规则更新【新规则引用了新的维度属性】的时候，清除旧缓存
    try {
      val dimFields: String = rule.dynamicDimensionField
      // 用户动态维度属性："user:nick, content: obj_type，admin_score"
      // json {
      //    维度属性: "starrocks_table: 维度补充字段， 限定主体和统计型 拼接 (value)"
      //    user: "hhz_member:nick,name & hhz_designer: type"
      //    content: "hhz_photos:score & hhz_article: xxx"
      //    bs: "hhz_like: value"
      //    brand: "cate_name"
      //
      // }
      // ================= 维度补充
      if (StringUtils.isBlank(dimFields)) return info
      val dimFieldsJson: JSONObject = JSON.parseObject(dimFields)

      var dm: String = null
      // 作者
      dm = dimFieldsJson.getString(IFTTT_DIMM_FIELD.DIM_AUTHOR)
      if (StringUtils.isNotBlank(dm)) {
        getDimValue(IFTTT_DIMM_FIELD.DIM_AUTHOR, dm, info, rule.ruleId)
      }


      // 内容
      dm = dimFieldsJson.getString(IFTTT_DIMM_FIELD.DIM_CONTENT)
      if (StringUtils.isNotBlank(dm)) {
        getDimValue(IFTTT_DIMM_FIELD.DIM_CONTENT, dm, info, rule.ruleId)
      }

      dm = dimFieldsJson.getString(IFTTT_DIMM_FIELD.DIM_BS)

      if (StringUtils.isNotBlank(dm)) {
        getDimValue(IFTTT_DIMM_FIELD.DIM_BS, dm, info, rule.ruleId, rule.bsId)
      }

      // 品牌
      dm = dimFieldsJson.getString(IFTTT_DIMM_FIELD.DIM_BRAND)
      if (StringUtils.isNotBlank(dm)) {
        val userActon: (String, String) = (info.getString("data_source"), info.getString("event"))
        val cates: (String, String) = getCates(userActon, info, cache, cache, jedis)
        if (StringUtils.isNoneBlank(cates._2)) {
          //          val param: JSONObject = info.getJSONObject("params")
          info.put("cate_name", cates._2)
          // brand 品牌固定 cate_name
        }
      }
      info
    } catch {
      case e: Exception => {
        e.printStackTrace()
        info
      }
    }
  }


  def getDimValue(dimField: String, dimContent: String, info: JSONObject, ruleId: Int, bsId: String = ""): Unit = {
    var srTb: String = ""
    var srFields: String = ""
    var pk: String = ""
    var dataPkValue: String = ""
    var dimValue: String = ""
    var dimWhereCondition: String = ""

    dimField match {
      case IFTTT_DIMM_FIELD.DIM_AUTHOR =>
        dataPkValue = info.getString("author_id")

      case IFTTT_DIMM_FIELD.DIM_CONTENT =>
        dataPkValue = info.getString("obj_id")
        if (dataPkValue.length != 16) return


      case IFTTT_DIMM_FIELD.DIM_BS =>
        // 获取业务维度 对应 pkValue
        val k: String = "%s_%s"
        dataPkValue = bsId match {
          case "0" => String.format(k, info.getString("uid"), info.getString("author_id"))
          case "1" => String.format(k, info.getString(
            "author_id"), info.getString("uid"))
          case "2" => info.getString("uid")
          case "3" => info.getString("author_id")
          case "4" => info.getString("oid")
          case _ => info.getString("uid")
        }
      case _ =>
        return
    }

    if (StringUtils.isBlank(dataPkValue)) return

    if (StringUtils.isNoneBlank(dimContent)) {
      val dimArray: JSONArray = JSON.parseArray(dimContent)
      dimArray.toJavaList(classOf[JSONObject]).forEach(new Consumer[JSONObject] {
        override def accept(t: JSONObject): Unit = {
          // 获取 维度扩充信息
          srTb = t.getString("tb")
          pk = t.getString("pk")
          srFields = t.getString("fields")
          dimWhereCondition = Option(t.getString("condition")).getOrElse("")
          // 维度补充
          dimValue = getCacheValue(ruleId, pk, dataPkValue, srTb, srFields, connection, dimWhereCondition,bsId)
          if (dimValue != null) {
            println(s"dimField: ${dimField}, dynamicDim ${dimValue}")
            info.put(dimField, dimValue)
          }
        }
      })
    }
  }


  /**
   * // 从缓存中获取值
   *
   * @param ruleId     维度key
   * @param primaryKey 主键rowKey
   * @return
   */
  def getCacheValue(ruleId: Int, primaryKey: String, rowValue: String, table: String, items: String, connection: Connection, dimWhereCondition: String,bsId: String ): String = {
    var value: String = null
    var pk: String = primaryKey
    val queryKey: String = ruleId + "_" + rowValue
    try {
      value = cache.getIfPresent(queryKey)
      if (value == null) {
        // todo 兼容业务属性
        if (StringUtils.isNoneBlank(bsId)) {
          bsId match {
            case "0" | "1" =>
              val pks: Array[String] = pk.split("_")
              if (pks.length == 2){
                pk = s"concat(cast (${pks(0)} as varchar),'_',cast(${pks(1)} as varchar))"
              }
            case _ =>
          }
        }
        val sql: String = s"select ${items} from ${table} where ${pk} = '${rowValue}' ${dimWhereCondition} limit 1"
        println("查询sql :" + sql)
        val statement: Statement = connection.createStatement()
        val set: ResultSet = statement.executeQuery(sql)
        val data: ResultSetMetaData = set.getMetaData
        val column: Int = data.getColumnCount
        var nObject: JSONObject = null
        while (set.next()) {
          nObject = new JSONObject()
          for (a <- 1 to column) {
            val columnName: String = data.getColumnName(a)
            nObject.put(columnName, set.getString(columnName))
          }
            cache.put(queryKey, nObject.toString)

        }
        if (nObject != null) {
          value = nObject.toJSONString
        }
        sRCounter.inc(1)
      } else {
        localCacheCounter.inc(1)
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


  def getCates(userAction: (String, String), value: JSONObject, noteIdCache: Cache[String, String], categoryCache: Cache[String, String], jedis: Jedis) = {

    var cates: (String, String) = ("", "")
    val params: String = value.getString("params")
    // 用户临时分数计算
    userAction match {

      // 搜索
      case ("action", "search") =>
        cates = (value.getString("obj_id"), value.getJSONObject("params").getString("tag"))

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

}
