package com.hhz.ifttt.pattern

import com.alibaba.fastjson.{JSON, JSONObject}
import com.hhz.ifttt.pattern.UserProfileDataPatternHandler.className
import com.hhz.ifttt.utils.{KafkaUtil, StreamExecutionEnvUtil}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.api.scala._

/**
 * @program ifttt 
 * @description: 匹配数据读取
 * @author: zyh 
 * @create: 2021/10/21 18:15 
 **/
object PatternRead {

  private val className: String = this.getClass.getSimpleName.replaceAll("\\$", "")

  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvUtil.getStreamExecutionEnv(className, isLocal = true)

    val sourceTopic: String = "dim_content"
    val seq: Seq[String] = Seq("8427009", "12838290","8809549","5471723")

    val kaConsumer: FlinkKafkaConsumer[String] = KafkaUtil.getKafkaSimpleConsumer(sourceTopic, className)
    kaConsumer.setStartFromLatest()

    val resStream: DataStream[String] = env.addSource(kaConsumer)

    resStream
//      .filter(info => {
//      val json: JSONObject = JSON.parseObject(info)
//      seq.contains(json.getString("uid"))
//    })
      .print()

    env.execute()
  }
}
