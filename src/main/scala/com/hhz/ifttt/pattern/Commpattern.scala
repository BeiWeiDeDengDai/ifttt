package com.hhz.ifttt.pattern

import java.util
import java.util.Properties
import java.util.concurrent.TimeUnit

import avro.shaded.com.google.common.cache.{Cache, CacheBuilder}
import com.alibaba.fastjson.{JSON, JSONObject}
import com.google.gson.Gson
import com.googlecode.aviator.{AviatorEvaluator, Expression}
import com.hhz.ifttt.pojo.Constants.IFTTT_COMMON_PATTER_PROCESS
import com.hhz.ifttt.pojo.{ActionLog, Constants, PvLog, ResLog, SensorsLog}
import com.hhz.ifttt.utils.UserActionTransUtil.UserInfoMap
import com.hhz.ifttt.utils.{ConvFunction, JedisPoolUtil, KafkaUtil, RedisUtil, TimeUtil}
import org.apache.commons.lang3.{ObjectUtils, StringUtils}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.{StateTtlConfig, ValueState, ValueStateDescriptor}
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.formats.avro.AvroDeserializationSchema
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.datastream.DataStreamSink
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}
import redis.clients.jedis.{Jedis, JedisPool}

import scala.collection.mutable.ArrayBuffer
import scala.collection.{JavaConversions, mutable}

/**
 * @program flinkcep 
 * @description: ${TODO}
 * @author: zhangyinghao
 * @create: 2021/04/23 16:19
 **/


object Commpattern {
  /**
   * 获取jedis连接
   *
   * @return
   */

//  val confInfo: (String, Int, String) = RedisUtil.getRedisHostAndPort("/home/resource_config/rds_cache.ini", "weighted")
  private val confInfo: (String, Int, String) = ("r-2zei8yj8s5zu85u6tm.redis.rds.aliyuncs.com", 6379, "hZrdS15zong")
  private val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  def getJedis(): Jedis = {
//    val jedisPool: JedisPool = JedisPoolUtil.getJedisPoolInstance(confInfo._1, confInfo._2, confInfo._3)
    //  val jedisPool: JedisPool = JedisPoolUtil.getJedisPoolInstance("172.16.7.3", 6379, "")
    logger.info("redis config" + confInfo)
    val jedis: Jedis = new Jedis(confInfo._1, confInfo._2)
    jedis.auth(confInfo._3)
    jedis
  }

  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(1000 * 60 * 2)



    println(s"redis conf: $confInfo")

    // 程序参数
    val params: ParameterTool = ParameterTool.fromArgs(args)
    val source: String = params.get("source")
    val granularity: Int = params.getInt("granularity", 1)
    val taskId: String = params.get("taskId")
    val name: String = params.get("name")
    env.setStateBackend(new FsStateBackend("oss://hhz-flink-tmp/flink-sessionclusters/namespaces/hhz-flink-default/sessionclusters/flink-etl/checkpoints/Commpattern/"+taskId))


    // 控制时间窗口
//    env.setStateBackend(new FsStateBackend(String.format(Constants.CHECKPOINT_URL, "Commpattern"+taskId)))
//    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)


    env.getConfig.setGlobalJobParameters(params)

    // 获取 source流
    val dataStream: DataStream[UserInfo] = getDataStreamSource(source, env, taskId).filter(_.action != "-1")
    // sink
    sinkToKafka(dataStream)


//    dataStream.print()

    // 数据打宽
    val value: DataStream[UserInfo] =
      //
      getKeyedStream(dataStream.process(new UserActionTypeFilter), granularity)
      .map(new SumFunction)




   value
      .map(new UserInfoMap)
//       .print()
      .addSink(KafkaUtil.getUserActionProducer())



    env.execute(name)

  }




  /**
   * 自定义求和
   */
  class SumFunction extends RichMapFunction[UserInfo, UserInfo] {
    private var sum: ValueState[Int] = _

    override def open(parameters: Configuration): Unit = {
      // 状态有效期 1天
      val ttlConfig: StateTtlConfig = StateTtlConfig
        .newBuilder(org.apache.flink.api.common.time.Time.hours(12))
        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
        .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
        .build

      val average: ValueStateDescriptor[Int] = new ValueStateDescriptor[Int]("average", classOf[Int])
      average.enableTimeToLive(ttlConfig)
      sum = getRuntimeContext.getState(
        average
      )
    }

    override def map(value: UserInfo): UserInfo = {
       val tmplastCnt: Int = sum.value
       var lastCnt: Int = if(tmplastCnt != null) tmplastCnt else 0
       val newSum: Int = value.cnt + lastCnt
       sum.update(newSum)
       value.copy(cnt = newSum)
    }
  }



  /** ********************************************************  case class pojo  **************************************************************************************/


  // 基础用户信息
  case class UserBase(uid: String, action: String, oid: String, author_id: String, cnt: Int = 1, params: String)

  // 用户信息
  case class UserInfo(source: String, uid: String, action: String, oid: String, author_id: String, userType: String, category: Array[String], cnt: Int = 1, time: Long, ruleId: Int, params: String = "", taskId: String = "")

  // 规则

  //    1. 类目关联
  //    2. 表达式
  //    3. 跳过
  case class Rule(id: String, source: String, types: String, actionWithCateName: String, computModel: Int = 1, patternstr: String = "", taskId: String = "")


  /** ********************************************************  custom function  **************************************************************************************/


  /**
   * 获取keyStream 聚合流
   * @param dataStream
   * @param granularity
   * @return
   */
  def getKeyedStream(dataStream: DataStream[UserInfo],granularity: Int) ={
    // case when 的方式 去选择
    // uid, action,
    // uid, author_id,
    // uid, action, author_id
    // uid, action, oid,
    granularity match{
      case 1 => dataStream.keyBy("uid","action")
      case 2 => dataStream.keyBy("uid", "author_id")
      case 3 => dataStream.keyBy("uid", "action", "author_id")
      case 4 => dataStream.keyBy("uid", "action", "oid")
    }
  }

  /**
   * sink2kafka
   *
   * @param dataStream
   * @return
   */
  def sinkToKafka(dataStream: DataStream[UserInfo]): DataStreamSink[String] = {
    val producerPro: Properties = KafkaUtil.getKafkaProduceProp(isNewKafka = true)
    val sinkTopic: String = "ifttt_commpattern_log"
    val producer: FlinkKafkaProducer[String] = new FlinkKafkaProducer[String](sinkTopic, new SimpleStringSchema(), producerPro)

    dataStream
      .map(info => {
        val json: Gson = new Gson()
        json.toJson(info)
      })
      .addSink(producer)
  }

  /**
   * 获取数据流
   *
   * @param source
   * @param env
   * @return
   */
  def getDataStreamSource(source: String, env: StreamExecutionEnvironment, consumer: String): DataStream[UserInfo] = {
    source.split(",")
      .map(source => {
        getDataStreamSourceBase(source, env, consumer)
      })
      .reduce((a, b) => a.union(b))
  }

  /**
   * 获取数据流
   *
   * @param source
   * @return
   */
  def getDataStreamSourceBase(source: String, env: StreamExecutionEnvironment, consumer: String): DataStream[UserInfo] = {
    var dataStream: DataStream[UserInfo] = null
    val properties: Properties = KafkaUtil.getKafkaConsumerProp(source + consumer, isNewKafka = true)

    source match {
      case "sensors" =>
        val sensorsSchema: AvroDeserializationSchema[SensorsLog] = AvroDeserializationSchema.forSpecific(classOf[SensorsLog])
        val sensorsConsumer: FlinkKafkaConsumer[SensorsLog] = new FlinkKafkaConsumer[SensorsLog]("dwd_sensors", sensorsSchema, properties)
        sensorsConsumer.setStartFromLatest()
        dataStream = env.addSource(sensorsConsumer).map(new SensorsUserInfoFill)

      case "action" =>
        val actionSchema: AvroDeserializationSchema[ActionLog] = AvroDeserializationSchema.forSpecific(classOf[ActionLog])
        val sensorsConsumer: FlinkKafkaConsumer[ActionLog] = new FlinkKafkaConsumer[ActionLog]("dwd_action_avro", actionSchema, properties)
        sensorsConsumer.setStartFromLatest()
        dataStream = env.addSource(sensorsConsumer)
//          .filter(_.getUid.toString.equals("8427009"))
          .map(new ActionUserInfoFill)

      case "pv" =>
        val pvSchema: AvroDeserializationSchema[PvLog] = AvroDeserializationSchema.forSpecific(classOf[PvLog])
        val sensorsConsumer: FlinkKafkaConsumer[PvLog] = new FlinkKafkaConsumer[PvLog]("dwd_pv", pvSchema, properties)
        sensorsConsumer.setStartFromLatest()
        dataStream = env.addSource(sensorsConsumer).map(new PvUserInfoFill)


      case "resclick" =>
        val resSchema: AvroDeserializationSchema[ResLog] = AvroDeserializationSchema.forSpecific(classOf[ResLog])
        val resClickConsumer: FlinkKafkaConsumer[ResLog] = new FlinkKafkaConsumer[ResLog]("resclicks_dwd_v2", resSchema, properties)
        resClickConsumer.setStartFromLatest()
        dataStream = env.addSource(resClickConsumer).map(new ResClickUserInfoFill())
//          .filter(_.uid == "8427009")
    }

    dataStream

  }


  /**
   * 用户类型 filter
   */
  class UserActionTypeFilter extends ProcessFunction[UserInfo, UserInfo] {

    private var ruleMap: util.Map[String, String] = new util.HashMap[String, String]()
    var scalaMap: mutable.Map[String, Rule] = _
    var expressionMap: mutable.Map[String, Expression] = _
    var jedisPool: JedisPool = _
    var jedis: Jedis = _


    val stringToExpression: util.HashMap[String, Expression] = new util.HashMap[String, Expression]()


    var sourceNumLines: LongCounter = new LongCounter()
    var sinkNumLines: LongCounter = new LongCounter()

    var expression: Expression = _

    var taskId: String = ""

    /**
     * 更新规则库
     */
    def update(taskId: String = ""): Unit = {
      ruleMap = jedis.hgetAll(Constants.IFTTT_RULE_INFO)
      // 提前编译表达式
      println("rule" + ruleMap)
      if (ruleMap.size() > 0) {
        scalaMap = JavaConversions.mapAsScalaMap(ruleMap).map(info => {
          val rule: JSONObject = JSON.parseObject(info._2)
          // 将同一个任务id的规则加进来
          if (taskId.equals(rule.getString("taskId"))) {
            // 如果计算模式为 模式串匹配
            if (rule.getInteger("computModel") == 2) {
              stringToExpression.put(rule.getString("id"), AviatorEvaluator.compile(rule.getString("pattern"), true))
            }
            (info._1, Rule(rule.getString("id"), rule.getString("source"),
              rule.getString("types"), rule.getString("actionWithCateName"),
              rule.getInteger("computModel"),
              rule.getString("pattern"), rule.getString("taskId"))
            )
          } else {
            (info._1,null)
          }
        })
          .filter(_._2 != null)
        expressionMap = JavaConversions.mapAsScalaMap(stringToExpression)

        println("scalaMap" + scalaMap)
        println("expressionMap" + expressionMap)
      }
    }

    override def open(parameters: Configuration): Unit = {

      getRuntimeContext.addAccumulator("source-num-lines", this.sourceNumLines)
      getRuntimeContext.addAccumulator("sink-num-lines", this.sinkNumLines)
      jedis = getJedis()
      taskId = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[ParameterTool].get("taskId")
      update(taskId)

    }

    override def close(): Unit = {
      if (jedis != null) {
        jedis.close()
      }
    }

    /**
     *
     * @param typeArray
     * @param value
     * @param actionWithCateName
     */
    def typeWithCategoryFilter(typeArray: Array[String], value: UserInfo, actionWithCateName: String): Boolean = {
      try{
        if (typeArray.contains(value.action)) {
          if (actionWithCateName.equals("")) return true
          // 过滤 基于动作的二级类目
          val actionWithCateNameArray: Array[String] = actionWithCateName.split(",")
          val result: Array[(String, Array[String])] = actionWithCateNameArray
            .map(item => {
              val strs: Array[String] = item.split(":")
              val action: String = strs(0)
              val names: Array[String] = strs(1).split("_")
              (action, names)
            })
            .filter(info => {
              val inters: Array[String] = info._2.intersect(value.category)
              info._1.equals(value.action) && inters.length > 0
            })

          if (result.length > 0) {
            sinkNumLines.add(1)
            return true
          } else return false
        }
      }catch {
        case e:Exception => {
          e.printStackTrace()
          return false
        }
      }

      false
    }

    override def processElement(value: UserInfo, ctx: ProcessFunction[UserInfo, UserInfo]#Context, out: Collector[UserInfo]): Unit = {

        if (sourceNumLines.getLocalValue > 1000000000) {
          sourceNumLines.resetLocal()
        }
        if (sinkNumLines.getLocalValue > 1000000000) {
          sinkNumLines.resetLocal()
        }
        sourceNumLines.add(1)

        // 检查更新
        //      if(IFTTT_COMMON_PATTER_PROCESS.IFTTT_UPDATE_INFO.equals(value.action)) update()


        var flag: Boolean = false
        // value.getRuleId,

        scalaMap.foreach(item => {
          val rule: Rule = item._2

          val typeArray: Array[String] = rule.types.split(",")

          // 根据规则计算模式去进行判断
          rule.computModel match {
            case 1 =>
              flag = typeWithCategoryFilter(typeArray, value, rule.actionWithCateName)
            case 2 =>
              try {
                val json: JSONObject = new JSONObject()
                json.put("uid", value.uid)
                json.put("action", value.action)
                json.put("author_id", value.author_id)
                json.put("oid", value.oid)
                json.put("userType", value.userType)
                json.put("source", value.source)
                json.put("ruleId", value.ruleId)
                json.put("params", JSON.parseObject(value.params))
                flag = expressionMap(rule.id).execute(json).toString.toBoolean
              } catch {
                case e: Exception => {
//                  e.printStackTrace()
                  flag = false
                }
              }
            case 3 => flag = true
          }
          if (flag) {
            sinkNumLines.add(1)
            out.collect(value.copy(ruleId = rule.id.toInt, taskId = rule.taskId))
          }
        })

      }
  }

  /**
   * 用户维度信息补全  --- 资源位 日志
   */
  class ResClickUserInfoFill extends RichMapFunction[ResLog, UserInfo] {
    var jedis: Jedis = _
    var ruleId: String = ""
    var array: ArrayBuffer[String] = new ArrayBuffer[String]()
    // note, wiki 对应 category缓存
    var noteIdCache: Cache[String, String] = CacheBuilder.newBuilder()
      .initialCapacity(40 * 1000)
      .maximumSize(40 * 1000)
      .expireAfterAccess(1, TimeUnit.DAYS)
      .build()

    // 商品品类 catename
    private val cateNameCache: Cache[String, String] = CacheBuilder.newBuilder()
      .initialCapacity(600)
      .maximumSize(1000)
      .expireAfterAccess(1, TimeUnit.DAYS)
      .build()

    // 用户类型cache
    private val userTypeCache: Cache[String, String] = CacheBuilder.newBuilder()
      .initialCapacity(4 * 1000)
      .maximumSize(40 * 1000)
      .expireAfterAccess(1, TimeUnit.DAYS)
      .build()


    override def open(parameters: Configuration): Unit = {
      jedis = getJedis()
      array = getRuleBuffer(jedis)
    }

    override def close(): Unit = {
      if (jedis != null) {
        jedis.close()
      }
    }

    override def map(value: ResLog): UserInfo = {

      val base: UserBase = UserBase(value.getUid.toString, value.getModule.toString, value.getObjId.toString, value.getObjUid.toString, params = value.getParams.toString)
      if (!array.contains(base.action)) return getDefaultUserInfo()
      // 根据用户id 获取用户身份

      try{
        // 从内容中提取 内容id
        val objId: String = getObjId(base, "pv")

        // 根据 objid 算作者id
        val authorId: String = getAuthorId(objId)

        // 拿取作者身份 (把非普通用户缓存起来)
        val authorType: String = getAuthorType(authorId, jedis, userTypeCache)
        var cateNames: Array[String] = null
        // 根据内容id获取类目
        cateNames = getCateNames(objId, jedis, noteIdCache, cateNameCache, value.getModule.toString)

        UserInfo("resclick", value.getUid.toString, value.getModule.toString, objId, authorId, authorType, cateNames, time = value.getTimestr.toString.toLong, ruleId = -1, params = value.getParams.toString)

      }catch {
        case e: Exception => {
          e.printStackTrace()
          return getDefaultUserInfo()
        }
      }

    }

  }

  /**
   * 用户维度信息补全  --- sensors 日志
   */
  class SensorsUserInfoFill extends RichMapFunction[SensorsLog, UserInfo] {
    var ruleId: String = ""
    var jedis: Jedis = _
    var array: ArrayBuffer[String] = new ArrayBuffer[String]()
    // note, wiki 对应 category缓存
    var noteIdCache: Cache[String, String] = CacheBuilder.newBuilder()
      .initialCapacity(40 * 1000)
      .maximumSize(40 * 1000)
      .expireAfterAccess(1, TimeUnit.DAYS)
      .build()

    // 商品品类 catename
    private val cateNameCache: Cache[String, String] = CacheBuilder.newBuilder()
      .initialCapacity(600)
      .maximumSize(1000)
      .expireAfterAccess(1, TimeUnit.DAYS)
      .build()

    // 用户类型cache
    private val userTypeCache: Cache[String, String] = CacheBuilder.newBuilder()
      .initialCapacity(4 * 1000)
      .maximumSize(40 * 1000)
      .expireAfterAccess(1, TimeUnit.DAYS)
      .build()


    override def open(parameters: Configuration): Unit = {
      jedis = getJedis()
      array = getRuleBuffer(jedis)
    }

    override def close(): Unit = {
      if (jedis != null) {
        jedis.close()
      }
    }

    override def map(value: SensorsLog): UserInfo = {

      val base: UserBase = UserBase(value.getUid.toString, value.getCategory.toString, value.getObjId.toString, null, params = "")
      if (!array.contains(base.action)) return getDefaultUserInfo()
      // 根据用户id 获取用户身份

      try{
        // 从内容中提取 内容id
        val objId: String = getObjId(base, "")

        // 根据 objid 算作者id
        val authorId: String = getAuthorId(objId)

        // 拿取作者身份 (把非普通用户缓存起来)
        val authorType: String = getAuthorType(authorId, jedis, userTypeCache)

        // 根据内容id获取类目
        val cateNames: Array[String] = getCateNames(objId, jedis, noteIdCache, cateNameCache, value.getCategory.toString)


        UserInfo("sensors", value.getUid.toString, value.getCategory.toString, objId, authorId, authorType, cateNames, time = value.getTime.toLong, ruleId = -1)

      } catch{
        case e: Exception => {
          e.printStackTrace()
          return getDefaultUserInfo()
        }
      }

    }
  }

  /** *
   * 用户维度信息补全  --- action 日志
   *
   */
  class ActionUserInfoFill extends RichMapFunction[ActionLog, UserInfo] {
    var jedis: Jedis = _
    var array: ArrayBuffer[String] = new ArrayBuffer[String]()
    // note, wiki 对应 category缓存
    var noteIdCache: Cache[String, String] = CacheBuilder.newBuilder()
      .initialCapacity(40 * 1000)
      .maximumSize(40 * 1000)
      .expireAfterAccess(1, TimeUnit.DAYS)
      .build()

    // 商品品类 catename
    private val cateNameCache: Cache[String, String] = CacheBuilder.newBuilder()
      .initialCapacity(600)
      .maximumSize(1000)
      .expireAfterAccess(1, TimeUnit.DAYS)
      .build()

    // 用户类型cache
    private val userTypeCache: Cache[String, String] = CacheBuilder.newBuilder()
      .initialCapacity(4 * 1000)
      .maximumSize(40 * 1000)
      .expireAfterAccess(1, TimeUnit.DAYS)
      .build()


    override def open(parameters: Configuration): Unit = {
      jedis = getJedis()
      array = getRuleBuffer(jedis)
    }

    override def close(): Unit = {
      if (jedis != null) {
        jedis.close()
      }
    }

    override def map(value: ActionLog): UserInfo = {

      val base: UserBase = UserBase(value.getUid.toString, value.getType.toString, "", null, params = value.getParams.toString)

      if (!array.contains(base.action)) return getDefaultUserInfo()
      // 根据用户id 获取用户身份

      try{
        // 从内容中提取 内容id
        val objId: String = getObjId(base, "action")

        // 根据 objid 算作者id
        val authorId: String = getAuthorId(objId)

        // 拿取作者身份 (把非普通用户缓存起来)
        val authorType: String = getAuthorType(authorId, jedis, userTypeCache)
        var cateNames: Array[String] = null
        // 根据内容id获取类目
        if (value.getType.toString.equals("search")) {
          cateNames = strToMap(value.getParams.toString).getOrElse("tag", "").split(",")
        } else {
          // 根据内容id获取类目
          cateNames = getCateNames(objId, jedis, noteIdCache, cateNameCache, value.getType.toString)
        }

        UserInfo("action", value.getUid.toString, value.getType.toString, objId, authorId, authorType, cateNames, time = value.getTime.toLong, ruleId = -1, params = getJSON(value.getParams.toString))


      }catch {
        case e: Exception => {
          e.printStackTrace()
          return getDefaultUserInfo()
        }
      }
    }

  }


  /**
   * 用户维度信息不全 ---- pv 日志
   */
  class PvUserInfoFill extends RichMapFunction[PvLog, UserInfo] {
    var jedis: Jedis = _
    var array: ArrayBuffer[String] = new ArrayBuffer[String]()
    // note, wiki 对应 category缓存
    var noteIdCache: Cache[String, String] = CacheBuilder.newBuilder()
      .initialCapacity(40 * 1000)
      .maximumSize(40 * 1000)
      .expireAfterAccess(1, TimeUnit.DAYS)
      .build()

    // 商品品类 catename
    private val cateNameCache: Cache[String, String] = CacheBuilder.newBuilder()
      .initialCapacity(600)
      .maximumSize(1000)
      .expireAfterAccess(1, TimeUnit.DAYS)
      .build()

    // 用户类型cache
    private val userTypeCache: Cache[String, String] = CacheBuilder.newBuilder()
      .initialCapacity(4 * 1000)
      .maximumSize(40 * 1000)
      .expireAfterAccess(1, TimeUnit.DAYS)
      .build()


    override def open(parameters: Configuration): Unit = {
      jedis = getJedis()
      array = getRuleBuffer(jedis)
    }

    override def close(): Unit = {
      if (jedis != null) {
        jedis.close()
      }
    }

    override def map(value: PvLog): UserInfo = {


      val base: UserBase = UserBase(value.getUid.toString, value.getType.toString, value.getOid.toString, null, params = "")
      if (!array.contains(base.action)) return getDefaultUserInfo()
      // 根据用户id 获取用户身份
      try {

        // 从内容中提取 内容id
        val objId: String = getObjId(base, "pv")

        // 根据 objid 算作者id
        val authorId: String = getAuthorId(objId)

        // 拿取作者身份 (把非普通用户缓存起来)
        val authorType: String = getAuthorType(authorId, jedis, userTypeCache)


        val cateNames: Array[String] = getCateNames(objId, jedis, noteIdCache, cateNameCache, value.getType.toString)


        UserInfo("pv", value.getUid.toString, value.getType.toString, objId, authorId, authorType, cateNames, time = TimeUtil.parseDateToTimestamp(value.getTime.toString, "yyyy-MM-dd HH:mm:ss") / 1000, ruleId = -1)

      } catch {
        case e: Exception => {
          e.printStackTrace()
          getDefaultUserInfo()
        }
      }
    }

  }


  /** ********************************************************  fun  **************************************************************************************/

  /**
   * 获取内容id
   *
   * @param userBase
   * @param source
   * @return
   */
  def getObjId(userBase: UserBase, source: String): String = {
    var objId: String = userBase.oid
    if (StringUtils.isBlank(userBase.oid)) {
      if (source.equals("action")) {
        objId = getObjId(userBase.params)
      }
    }
    objId
  }

  /**
   * 获取作者id
   *
   * @param objId
   * @return
   */
  def getAuthorId(objId: String): String = {
    var author_uid: String = ""
    try {
      if (StringUtils.isNotBlank(objId) && objId.length == 16) {
        val str: String = objId.substring(10)
        author_uid = ConvFunction.transRadix(str, 36, 10)
      }
      author_uid
    } catch {
      case ex: Exception => {
        ex.printStackTrace()
        author_uid
      }
    }
  }


  /**
   * 获取作者身份
   *
   * @param authorId
   * @return
   */
  def getAuthorType(authorId: String, jedis: Jedis, userTypeCache: Cache[String, String]): String = {
    var userType: String = userTypeCache.getIfPresent(authorId)

    if (StringUtils.isEmpty(userType)) {
      userType = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_USER_INFO, authorId)

      if (StringUtils.isNotBlank(userType)) {
        userTypeCache.put(authorId, userType)
      } else {
        userType = "0"
      }
    }
    userType
  }


  /**
   * 获取品牌类目
   *
   * @param objId
   * @return
   */
  def getCateNames(objId: String, jedis: Jedis, noteIdCahe: Cache[String, String], cateNameCahe: Cache[String, String], action: String): Array[String] = {
    var id: String = objId



    // 首先从缓存中获取

    if (action.contains("wiki_ranking")) {
      id = "wiki_ranking_" + objId
    }
    var contentName: String  = noteIdCahe.getIfPresent(id)



    //    val a: String = "茶几,沙发"
    var strings: Array[String] = new Array[String](1)


    // 尝试获取 类目id
    if (!StringUtils.isNotBlank(contentName)) {
      if (action.contains("wiki_ranking")) contentName = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_RANK_NOTE_CATEGORY, objId)
       else contentName = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_NOTE_CATEGORY, id)


      if (!StringUtils.isNotBlank(contentName)) {
        contentName = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_WIKI_CATEGORY, id)
      }


    }

    // 根据类目id, 获取类目名称
    if (StringUtils.isNotBlank(contentName)) {
      noteIdCahe.put(id, contentName)
      // contentName 可能是多个
      val cateNames: Array[String] = contentName.split(",")
      if (cateNames.length > 0) {
        val cs: Array[String] = cateNames.map(cate => {
          var cateName: String = ""
          cateName = cateNameCahe.getIfPresent(cate)
          // 如果缓存中为空则 从redis拿取
          if (!StringUtils.isNotBlank(cateName)) {
            cateName = jedis.hget(IFTTT_COMMON_PATTER_PROCESS.IFTTT_DATA_CATEGORY, cate)
          }
          if (StringUtils.isNotBlank(cateName)) {
            cateNameCahe.put(cate, cateName)
          }
          cateName
        })
          .filter(StringUtils.isNotBlank(_))
          .map(_.trim)
        return cs
      }
    }
    return strings
  }


  /**
   * 从params中获取id
   *
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

    // 榜单  wiki
    val wikiID: String = paramsMap.getOrElse("wikiID", null)
    val wiki_id: String = paramsMap.getOrElse("wiki_id", null)
    val ranking_id: String = paramsMap.getOrElse("ranking_id", null)


    ObjectUtils.firstNonNull(objId, articleId, activity_id, ideabook_id, photo_id, answer_id, question_id, blank_id, wikiID, ranking_id, wiki_id,"")

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




  /**
   * 获取规则
   *
   * @param jedis
   * @return
   */
  def getRuleBuffer(jedis: Jedis) = {
    var array: ArrayBuffer[String] = new ArrayBuffer[String]()
    val rulesInfo: util.Map[String, String] = jedis.hgetAll(Constants.IFTTT_RULE_INFO)
    JavaConversions.mapAsScalaMap(rulesInfo).foreach(info => {
      val rule: JSONObject = JSON.parseObject(info._2)
      val types: Array[String] = rule.getString("types").split(",")
      types.foreach(array.append(_))
    })
    array.distinct
  }


  /**
   * 默认
   *
   * @return
   */
  def getDefaultUserInfo(): UserInfo = {
    UserInfo("", "", "-1", "", "", "", new Array[String](1), 1, -1, -1)
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
}
