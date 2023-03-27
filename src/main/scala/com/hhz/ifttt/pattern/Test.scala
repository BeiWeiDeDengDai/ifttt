package com.hhz.ifttt.pattern

import java.util.Date
import java.{lang, util}

import com.alibaba.fastjson.{JSON, JSONArray, JSONObject}
import com.google.gson.Gson
import com.hhz.ifttt.pojo.Constants
import com.hhz.ifttt.pojo.Pojos.PatternResult
import com.hhz.ifttt.utils.StreamExecutionEnvUtil
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import redis.clients.jedis.Jedis
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessAllWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
/**
 * @program ifttt 
 * @description: ${TODO} 
 * @author: zhangyinghao 
 * @create: 2022/01/17 13:52 
 **/
object Test {

    def main(args: Array[String]): Unit = {
      val env: StreamExecutionEnvironment = StreamExecutionEnvUtil.getStreamExecutionEnv("aa")


    }

  case class T(id: Int, name: String)
}
