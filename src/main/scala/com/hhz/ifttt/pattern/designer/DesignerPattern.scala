package com.hhz.ifttt.pattern.designer

import java.util
import java.util.Date
import java.util.concurrent.TimeUnit

import avro.shaded.com.google.common.cache.{Cache, CacheBuilder}
import com.alibaba.fastjson.{JSON, JSONObject}
import com.google.gson.Gson
import com.hhz.ifttt.pattern.RulePatternHandler.getSourceStream
import com.hhz.ifttt.pojo.Constants
import com.hhz.ifttt.pojo.Pojos.PatternResult
import com.hhz.ifttt.utils.{KafkaUtil, RedisUtil, StreamExecutionEnvUtil}
import org.apache.commons.lang3.StringUtils
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}
import redis.clients.jedis.Jedis


/**
 * @program ifttt 
 * @description: 设计师ifttt优化需求
 * @author: zhangyinghao 
 * @create: 2022/01/04 13:37 
 **/
object DesignerPattern {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)
  private val className: String = this.getClass.getSimpleName.replaceAll("\\$", "")
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvUtil.getStreamExecutionEnv(className)
    // 事件流
    val eventStream: DataStream[String] = getSourceStream(env, className, "dwd_ifttt_user_action")

    val sink: FlinkKafkaProducer[String] = KafkaUtil.getKafkaProduce("flink_cep_pattern_result", isNewKafka = true)


    eventStream
      //      .filter(info => {
      //      JSON.parseObject(info).getString("uid").equals("8427009")
      //    })
      .process(new DesignerPatternProcessFunction)
      .addSink(sink)
      .name("sink:flink_cep_pattern_result")

    env.execute(className)
  }


  /**
   * 设计师处理
   */
  class DesignerPatternProcessFunction extends ProcessFunction[String, String] {
    //  redis 连接配置
//    val redisConfInfo: (String, Int, String) = RedisUtil.getRedis()
        val redisConfInfo: (String, Int, String) = RedisUtil.getRedisHostAndPort(Constants.REDIS_PATH, Constants.REDIS_FLAG)
    var jedis: Jedis = _

    var baseUserTypeCache: Cache[String, String] = CacheBuilder.newBuilder()
      .concurrencyLevel(3)
      .initialCapacity(4 * 1000)
      .maximumSize(5 * 1000)
      .expireAfterAccess(2, TimeUnit.DAYS)
      .build()


    override def open(parameters: Configuration): Unit = {

      jedis = new Jedis(redisConfInfo._1, redisConfInfo._2)
      //        jedis = new Jedis("hadoop10",6379)
                  jedis.auth(redisConfInfo._3)
      logger.info("redis conf:" + redisConfInfo)
    }

    override def close(): Unit = {
      if (jedis != null) {
        jedis.close()
      }
    }


    private val actions: Seq[String] = Seq("like", "favorite", "comment", "follow", "share")

    override def processElement(value: String, ctx: ProcessFunction[String, String]#Context, out: Collector[String]): Unit = {
      try {
        val data: JSONObject = JSON.parseObject(value)
        val dataSource: String = data.getString("dataSource")
        val event: String = data.getString("event")
        var actFrom: String = ""
        try {
          val params: JSONObject = data.getJSONObject("params")
          actFrom = Option(params.getString("act_from")).getOrElse("")
        } catch {
          case e: com.alibaba.fastjson.JSONException => {
            actFrom = ""
          }
        }

        // ifttt_task_2
        if (dataSource.equals(Constants.IFTTT_LOG.LOG_SENSORS) && actions.contains(event) && !"Suggest".equals(actFrom)) {
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
          // 如果为设计师 进行分发
          if ("2".equals(userType)) {
            val map: util.HashMap[String, String] = new util.HashMap[String, String](1)
            map.put("begin", value)
            out.collect(new Gson().toJson(PatternResult("ifttt_task_2", data.getString("uid"), new Date().toString, map, "")))
          }
        }
      } catch {
        case e: Exception => {
          e.printStackTrace()
          logger.error("parse error, msg: " + e.getMessage + ", value: " + value)
        }
      }


    }
  }

}
