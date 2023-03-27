package com.hhz.ifttt.pattern.v3

import com.alibaba.fastjson.{JSON, JSONObject}
import com.googlecode.aviator.{AviatorEvaluator, Expression}
import com.hhz.ifttt.pattern.PatternRead.className
import com.hhz.ifttt.pojo.Pojos.OutLog
import com.hhz.ifttt.utils.{KafkaUtil, StreamExecutionEnvUtil}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.api.scala._

object Test {
  private val className: String = this.getClass.getSimpleName.replaceAll("\\$", "")
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvUtil.getStreamExecutionEnv(className, isLocal = true)

    val sourceTopic: String = "ifttt_out_log"
    val kaConsumer: FlinkKafkaConsumer[String] = KafkaUtil.getKafkaSimpleConsumer(sourceTopic, className)
    kaConsumer.setStartFromLatest()

    val resStream: DataStream[String] = env.addSource(kaConsumer)

    resStream
        .map(t => {
          val log: OutLog = JSON.parseObject(t, classOf[OutLog])
          (log.msg, log.uid, log.event,log.data)
        }
        )

      .print()

    env.execute()
  }


  def aa(info: JSONObject): Unit ={
    info.put("a","b")
  }

}
