package com.hhz.ifttt.functions

import java.{lang, util}
import java.util.Date

import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}
import com.google.gson.Gson
import com.hhz.ifttt.pojo.Constants
import com.hhz.ifttt.pojo.Pojos.PatternResult
import com.hhz.ifttt.utils.RedisUtil
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.flink.util.Collector
import org.slf4j.{Logger, LoggerFactory}
import redis.clients.jedis.Jedis


/**
 * @program ifttt 
 * @description: 用户画像处理类
 * @author: zyh 
 * @create: 2021/10/13 14:58 
 **/
class UserProfileDataProcessFunction extends ProcessFunction[String, String] {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  val redisConfInfo: (String, Int, String) = ("r-2zeyxh2w62nxpq7q0h.redis.rds.aliyuncs.com", 6379, "hZrdS15zong")
//    RedisUtil.getRedisHostAndPort(Constants.REDIS_PATH, Constants.REDIS_FLAG)
//    val redisConfInfo: (String, Int, String) = RedisUtil.getRedis()

  var jedis: Jedis = _

  override def open(parameters: Configuration): Unit = {
    jedis = new Jedis(redisConfInfo._1, redisConfInfo._2)
    jedis.auth(redisConfInfo._3)
    logger.info("redis conf:" + redisConfInfo)
  }


  override def close(): Unit = {
    if (jedis != null) {
      jedis.close()
    }
  }


  override def processElement(value: String, ctx: ProcessFunction[String, String]#Context, out: Collector[String]): Unit = {
    // 用户id
    // taskId
    try {
      val data: JSONObject = JSON.parseObject(value)
      val userData: JSONObject = data.getJSONObject("data")

      val uid: String = data.getString("uid")
      val taskId: String = data.getString("groupId")

      val key: String = String.format(Constants.IFTTT_USER_PROFILE_DATA_KEY, taskId)
      val isExists: lang.Boolean = jedis.hexists(key, uid)
      if (isExists) {
        val array: JSONArray = new JSONArray()
        val ud: JSONObject = userData.getJSONObject("data")
        val params: JSONObject = ud.getJSONObject("params")
        ud.put("params", params)
        array.add(ud)

        val map: util.HashMap[String, String] = new util.HashMap[String, String](1)
        map.put("begin", array.toJSONString)
        out.collect(new Gson().toJson(PatternResult("ifttt_task_" + taskId, uid, new Date().toString, map, "")))
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }
  }

}
