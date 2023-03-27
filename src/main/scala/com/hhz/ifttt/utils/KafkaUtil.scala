package com.hhz.ifttt.utils

import java.util.{Optional, Properties}

import com.hhz.ifttt.serialization.{ResKafkaDeserializationSchema, UserActionKeyedSerializationSchema}
import org.apache.avro.specific.SpecificRecord
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.formats.avro.AvroDeserializationSchema
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import com.hhz.ifttt.utils.KafkaUtil.getKafkaConsumerProp
import com.hhz.ifttt.pojo.{CommonLog, Constants, UserAction}
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.java.utils.ParameterTool

/**
 * @program flinkdemo
 * @description: 分区动态发现 consumer设置 ：flink.partition-discovery.interval-millis  非值 默认为falase, topic描述最好使用正则匹配
 *
 *
 *
 *               commit offset 方式
 *               Flink kafka consumer commit offset 方式需要区分是否开启了 checkpoint。
 *               如果 checkpoint 关闭，commit offset 要依赖于 kafka 客户端的 auto commit。
 *                   设置 enable.auto.commit，auto.commit.interval.ms 参数到 consumer properties，就会按固定的时间间隔定期 auto commit offset 到 kafka。
 *               如果开启 checkpoint，这个时候作业消费的 offset 是 Flink 在 state 中自己管理和容错。此时提交 offset 到 kafka，
 *               一般都是作为外部进度的监控，想实时知道作业消费的位置和 lag 情况。此时需要 setCommitOffsetsOnCheckpoints 为 true 来设置当 checkpoint
 *               成功时提交 offset 到 kafka。此时 commit offset 的间隔就取决于 checkpoint 的间隔，
 *               所以此时从 kafka 一侧看到的 lag 可能并非完全实时，如果 checkpoint 间隔比较长 lag 曲线可能会是一个锯齿状。
 *
 *
 *               kafka Produce :
 *                0.9 and 0.10:
 *                  setLogFailuresOnly: 默认 false. 写失败时，是否只打印失败log, 不抛异常
 *                  setFlushOnCheckpoint: 默认 true. checkpoint时保证数据写到kafka
 *                  at-least-once语义： setLogFailuresOnly: false + setFlushOnCheckpoint: true
 *
 * @author: zhangyinghao 
 * @create: 2020/03/27 18:12 
 **/
class KafkaUtil[T<: SpecificRecord] {



  // 返回Flink Kafka Consumer
  def getKafkaConsumer(sourceTopic: String, consumerId: String, schemaClass: Class[T], isNewKafka: Boolean = false): FlinkKafkaConsumer[T] = {
    val schema: AvroDeserializationSchema[T] = AvroDeserializationSchema.forSpecific(schemaClass)
    val consumer: FlinkKafkaConsumer[T] = new FlinkKafkaConsumer[T](sourceTopic, schema, getKafkaConsumerProp(consumerId, isNewKafka))
    consumer.setStartFromLatest()
    consumer
  }

}
object KafkaUtil {

  def consumerResetLog(consumer: FlinkKafkaConsumer[CommonLog], args: Array[String]): FlinkKafkaConsumer[CommonLog] ={
    // 判断是否回溯
    if (args.length > 0) {
      val tool: ParameterTool = ParameterTool.fromArgs(args)
      val time: String = tool.get("time")
      if (StringUtils.isNotBlank(time)){
        time match {
          case "0" => consumer.setStartFromEarliest()
          case "-1" => consumer.setStartFromLatest()
          case _ => consumer.setStartFromTimestamp(time.toLong * 1000L)
        }
      }
    }
    consumer
  }
  def getKafkaSimpleConsumerLog(sourceTopic: String, consumerId: String, isNewKafka: Boolean = false): FlinkKafkaConsumer[CommonLog] = {
    //    val consumer: FlinkKafkaConsumer[String] = new FlinkKafkaConsumer[String](sourceTopic, new SimpleStringSchema(), getKafkaConsumerProp(consumerId,isNewKafka))
    val consumer: FlinkKafkaConsumer[CommonLog] = new FlinkKafkaConsumer[CommonLog](sourceTopic, new ResKafkaDeserializationSchema, getKafkaConsumerProp(consumerId,isNewKafka))

    consumer
  }
  def consumerReset(consumer: FlinkKafkaConsumer[String], args: Array[String]): FlinkKafkaConsumer[String] ={
    // 判断是否回溯
    if (args.length > 0) {
      val tool: ParameterTool = ParameterTool.fromArgs(args)
      val time: String = tool.get("time")
      if (StringUtils.isNotBlank(time)){
        time match {
          case "0" => consumer.setStartFromEarliest()
          case "-1" => consumer.setStartFromLatest()
          case _ => consumer.setStartFromTimestamp(time.toLong * 1000L)
        }
      }
    }
    consumer
  }
  def getKafkaSimpleConsumer(sourceTopic: String, consumerId: String, isNewKafka: Boolean = false): FlinkKafkaConsumer[String] = {
    val consumer: FlinkKafkaConsumer[String] = new FlinkKafkaConsumer[String](sourceTopic, new SimpleStringSchema(), getKafkaConsumerProp(consumerId, isNewKafka))
    consumer
  }
  /**
   * 获取UserActionProducer
   */
  def getUserActionProducer(): FlinkKafkaProducer[UserAction] = {
    val sinkTopic: String = "dwd_user_action"
    val kafkaProducer: FlinkKafkaProducer[UserAction] = new FlinkKafkaProducer[UserAction](
      sinkTopic,
      new UserActionKeyedSerializationSchema(),
      KafkaUtil.getKafkaProduceProp(isNewKafka = true),
      Optional.of(new FlinkKafkaPartitioner[UserAction]() {
        override def partition(record: UserAction, key: Array[Byte], value: Array[Byte], targetTopic: String, partitions: Array[Int]): Int =
          Math.abs(new String(key).hashCode % partitions.length)
      })
    )
    kafkaProducer
  }

  def getKafkaConsumerProperties(kafkaBrokers: String, groupId: String, autoOffset: String): Properties = {
    val properties = new Properties()



    properties.setProperty("bootstrap.servers", kafkaBrokers)
    properties.setProperty("group.id", groupId)
    properties.setProperty("auto.offset.reset", autoOffset)
    //properties.put("enable.auto.commit", true)
    //properties.put("auto.commit.interval.ms", "1000")
    //properties.put("max.poll.records", 10000)
    properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    properties
  }


  /**
   * 获取sinkTopic
   *
   * @param sinkTopic
   * @return
   */
  def getKafkaProduce(sinkTopic: String,isNewKafka: Boolean = false): FlinkKafkaProducer[String] = {
    val prop: Properties = getKafkaProduceProp(isNewKafka)
    val producer: FlinkKafkaProducer[String] = new FlinkKafkaProducer[String](sinkTopic, new SimpleStringSchema(), prop)
    producer
  }

  /**
   * 获取生产者Prop
   *
   * @return
   */
  def getKafkaProduceProp(isNewKafka: Boolean = false) = {
    val prop: Properties = new Properties()
    var broker: String = Constants.BROKERS
    if(isNewKafka) {
      broker = Constants.NEW_BROKERS
    }
    prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, broker)
    prop
  }

  /**
   * 获取Kafka Consumer prop
   *
   * @return properties
   */
  def getKafkaConsumerProp(groupId: String = "other", isNewKafka: Boolean = false) = {
    val prop: Properties = new Properties()
    var broker: String = Constants.BROKERS
    if(isNewKafka) {
//      broker = Constants.NEW_BROKERS
      broker = "alikafka-post-cn-zvp2f7qo4002-1-vpc.alikafka.aliyuncs.com:9092,alikafka-post-cn-zvp2f7qo4002-2-vpc.alikafka.aliyuncs.com:9092,alikafka-post-cn-zvp2f7qo4002-3-vpc.alikafka.aliyuncs.com:9092"
    }
    // 发现分区
    prop.put("flink.partition-discovery.interval-millis", "10000")
    prop.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, broker  )
    prop.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    prop
  }

  def getKafkaIMConsumerProp(groupId: String = "other") = {
    val prop: Properties = new Properties();
    prop.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "172.17.200.101:9092,172.17.200.102:9092,172.17.200.103:9092")
    prop.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    prop
  }


  def getAvroProduceProp(groupId: String = "other") = {
    val prop: Properties = new Properties()
    prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, Constants.BROKERS)
    prop.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest") // earliest latest none
    prop
  }
}