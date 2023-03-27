package com.hhz.ifttt.pattern.v3

import com.alibaba.fastjson.JSON
import com.google.gson.Gson
import com.hhz.ifttt.functions.v3.{CompletePatternProcessFunction, DynamicGroupProcessFunction}
import com.hhz.ifttt.pojo.{Constants, Pojos}
import com.hhz.ifttt.pojo.Pojos.RuleEvent
import com.hhz.ifttt.utils.{KafkaUtil, StreamExecutionEnvUtil}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer

/**
 * @program ifttt 
 * @description: 用户完成A事件
 * @author: zhangyinghao 
 * @create: 2023/01/09 14:37 
 **/
object UserPatternCompleteA {
  private val className: String = this.getClass.getSimpleName.replaceAll("\\$", "")

  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvUtil.getStreamExecutionEnv(className, isLocal = true)

    val completeStream: DataStream[String] = StreamExecutionEnvUtil.getKafkaSource(env, Constants.SINK_TOPIC.COMPLETE_STREAM, className, args)

    val resultStream: DataStream[String] = completeStream
      .process(new DynamicGroupProcessFunction)
      .keyBy(_._1) // 动态 key 分组
      .process(new CompletePatternProcessFunction)

    val producer: FlinkKafkaProducer[String] = KafkaUtil.getKafkaProduce(Constants.SINK_TOPIC.IFTTT_RES)

    val outLogProducer: FlinkKafkaProducer[String] = KafkaUtil.getKafkaProduce(Constants.SINK_TOPIC.OUT_LOG)
    resultStream.getSideOutput(Pojos.iftttOutLog).map(new Gson().toJson(_)).addSink(outLogProducer).name("ifttt_out_log")


    resultStream.print("匹配完成： ")


    env.execute()

  }
}
