package com.hhz.ifttt.pattern

import com.hhz.ifttt.functions.UserProfileDataProcessFunction
import com.hhz.ifttt.utils.{KafkaUtil, StreamExecutionEnvUtil}
import org.apache.avro.LogicalTypes.Date
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.slf4j.{Logger, LoggerFactory}
import org.apache.flink.api.scala._

/**
 * @program ifttt 
 * @description: 用户画像数据匹配
 * @author: zyh 
 * @create: 2021/10/13 11:42 
 **/
object UserProfileDataPatternHandler {

  private val className: String = this.getClass.getSimpleName.replaceAll("\\$", "")

  def main(args: Array[String]): Unit = {

    val env: StreamExecutionEnvironment = StreamExecutionEnvUtil.getStreamExecutionEnv(className, isOSS = true)

    val sourceTopic: String = "dwd_ifttt_user_profile_data"
    //    System.setProperty("HADOOP_USER_NAME", "hdfs")

    val kaConsumer: FlinkKafkaConsumer[String] = KafkaUtil.getKafkaSimpleConsumer(sourceTopic, className, isNewKafka = true)
    kaConsumer.setStartFromLatest()
    val profileStream: DataStream[String] = env.addSource(kaConsumer)


    var sinkTopic: String = "flink_cep_pattern_result"
    //    var sinkTopic: String = "flink_test"
    val sink: FlinkKafkaProducer[String] = KafkaUtil.getKafkaProduce(sinkTopic, isNewKafka = true)

    val resultStream: DataStream[String] = profileStream
      .process(new UserProfileDataProcessFunction())
      .name("UserProfileDataProcessFunction")
    resultStream.addSink(sink)


    env.execute(className)


  }
}
