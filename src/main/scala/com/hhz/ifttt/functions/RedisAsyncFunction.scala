package com.hhz.ifttt.functions

import com.alibaba.fastjson.{JSON, JSONObject}
import com.hhz.ifttt.pojo.Pojos.RuleEvent
import com.hhz.ifttt.utils.RedisUtil
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala.async.{AsyncFunction, ResultFuture, RichAsyncFunction}
import redis.clients.jedis.Jedis

/**
 * @program ifttt 
 * @description: 异步io方式   丢弃不使用
 * @author: zyh 
 * @create: 2021/09/26 17:50 
 **/
@Deprecated
class RedisAsyncFunction extends RichAsyncFunction[RuleEvent, RuleEvent] {

  // redis
  //  val redisConfInfo: (String, Integer, String) = RedisUtil.getRedisHostAndPort("/home/resource_config/rds_recommend.ini", "season_content")


  val redisConfInfo: (String, Int, String) = RedisUtil.getRedis()

  var jedis: Jedis = _

  override def open(parameters: Configuration): Unit = {
    jedis = new Jedis(redisConfInfo._1, redisConfInfo._2)
    jedis.auth(redisConfInfo._3)
  }


  override def close(): Unit = {
    if (jedis != null) {
      jedis.close()
    }
  }

  override def asyncInvoke(ruleEvent: RuleEvent, resultFuture: ResultFuture[RuleEvent]): Unit = {

    val data: JSONObject = JSON.parseObject(ruleEvent.data)

    try {
        // 维度补全
        var userType: String = jedis.hget("user_type", data.getString("authorId"))
        // 如果获取不到则表名未普通用户
        if (userType == null) {
          userType = "0"
        }
        data.put("authorUserType", userType)

        var objType: String = jedis.hget("obj_type", data.getString("oid"))
        if (objType == null) objType = "-1"
        data.put("objType", objType)
        resultFuture.complete(Iterable(ruleEvent.copy(data = data.toJSONString)))
    } catch {
      case e: Exception => {
        e.printStackTrace()
        resultFuture.complete(Iterable(ruleEvent))
      }
    }
  }
}
