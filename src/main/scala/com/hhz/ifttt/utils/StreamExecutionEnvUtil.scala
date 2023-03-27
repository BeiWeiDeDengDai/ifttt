package com.hhz.ifttt.utils

import com.hhz.ifttt.pojo.{CommonLog, Constants}
import org.apache.flink.api.scala._
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

/**
 * @program realtime 
 * @description: 实时流环境构建
 * @author: zhangyinghao 
 * @create: 2021/07/06 11:01 
 **/
object StreamExecutionEnvUtil {

  /**
   * 获取环境变量
   *
   * @param checkPointPathVariable
   * @param checkPointTime
   * @return
   */
  def getStreamExecutionEnv(checkPointPathVariable: String, checkPointTime: Long = 20, isLocal: Boolean = false, isOSS: Boolean = false): StreamExecutionEnvironment = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
//    System.setProperty("HADOOP_USER_NAME", "hdfs")
    if (!isLocal){
      env.enableCheckpointing(1000 * checkPointTime)
    }
    if (SystemUtil.isLinux()) {
      var url: String = if (isOSS) Constants.CHECKPOINT_OSS_URL else Constants.CHECKPOINT_URL
      env.setStateBackend(new FsStateBackend(String.format(url, checkPointPathVariable)))
      //      env.setStateBackend(new FsStateBackend(String.format(Constants.CHECKPOINT_OSS_URL, checkPointPathVariable)))
    }
    env.getCheckpointConfig.enableExternalizedCheckpoints(RETAIN_ON_CANCELLATION)
//    env.setStateBackend(new FsStateBackend(String.format("file:///Users/zhangyinghao/bigdata/ifttt/checkpoints/%s", checkPointPathVariable)))
    env
  }


  def getKafkaSourceCommonLog(env: StreamExecutionEnvironment, sourceTopic: String,consumerId: String, args: Array[String]): DataStream[CommonLog] = {
    var consummer: FlinkKafkaConsumer[CommonLog] = KafkaUtil.getKafkaSimpleConsumerLog(sourceTopic, consumerId)
    consummer = KafkaUtil.consumerResetLog(consummer, args)
    val stream: DataStream[CommonLog] = env.addSource(consummer)
    stream
  }

  /**
   * 获取Kafka source
   * @param env
   * @param sourceTopic
   * @param consumerId
   * @param args
   * @return
   */
  def getKafkaSource(env: StreamExecutionEnvironment, sourceTopic: String,consumerId: String, args: Array[String]): DataStream[String] = {
    var consummer: FlinkKafkaConsumer[String] = KafkaUtil.getKafkaSimpleConsumer(sourceTopic, consumerId)
    consummer = KafkaUtil.consumerReset(consummer, args)
    val stream: DataStream[String] = env.addSource(consummer)
      .name(sourceTopic)
    stream
  }


  /**
   * 获取测试流
   * @param env
   * @param host
   * @param port
   * @return
   */
  def getTestStream(env: StreamExecutionEnvironment, host: String, port: Int): DataStream[String] = {
    env.socketTextStream(host, port)
  }







}
