package com.hhz.ifttt.logs

import com.alibaba.fastjson.{JSON, JSONObject}
import com.hhz.ifttt.logs.BaseLogMerge.className
import com.hhz.ifttt.pattern.RulePatternHandler.{className, getSourceStream}
import com.hhz.ifttt.pojo.Constants.SINK_TOPIC
import com.hhz.ifttt.pojo.PvLog
import com.hhz.ifttt.utils.{KafkaUtil, StreamExecutionEnvUtil}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
/**
 * @program ifttt 
 * @description: ${TODO} 
 * @author: zhangyinghao 
 * @create: 2022/01/04 11:18 
 **/
object LogRead {


  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvUtil.getStreamExecutionEnv("className", isLocal = true)
//    // 事件流
//    val eventStream: DataStream[String] = getSourceStream(env, "aaa", "dwd_ifttt_user_action")
//          .filter(info => {
//            val a: JSONObject = JSON.parseObject(info)
//            a.getString("uid") == "8427009"
//          })
//
//
//    eventStream.print()

    val pvConsumer: DataStream[String] =StreamExecutionEnvUtil.getKafkaSource(env,"dwd_ifttt_user_action", "className",  args)

    pvConsumer.filter(_.contains("8427009")).print()


    env.execute()
  }

}
