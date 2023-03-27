package com.hhz.ifttt.logs

import java.util.Properties

import com.alibaba.fastjson.{JSON, JSONObject}
import com.google.gson.Gson
import com.hhz.ifttt.functions.{ActionUserInfoFillFunction, PvUserInfoFillFunction, ResClickUserInfoFillFunction, ResShowUserInfoFillFunction, SensorsUserInfoFillFunction, UserAccessExtractProcessFunction, UserAccessKeyedProcessFunction}
import com.hhz.ifttt.pojo.Constants.{IFTTT_LOG, SINK_TOPIC}
import com.hhz.ifttt.pojo.{ActionLog, CommonLog, Constants, NginxLog, Pojos}
import com.hhz.ifttt.pojo.Pojos.{IftttUserInfo, PvLog, ResShowLog, SensorsLog}
import com.hhz.ifttt.utils.{KafkaUtil, StreamExecutionEnvUtil}
import org.apache.flink.streaming.api.scala.{DataStream, OutputTag, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
/**
 * @program ifttt 
 * @description: iftttBaseLog合并
 * @author: zyh 
 * @create: 2021/09/13 15:32 
 **/
object BaseLogMerge {

  private val className: String = this.getClass.getSimpleName.replaceAll("\\$", "")

  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvUtil.getStreamExecutionEnv(className, isOSS = true)

    val logs: String = "sensors,action,pv,resshow,resclick,nginx"
//                val logs = "action"
    // sensors,action,pv,resshow,resclick,nginx
    val dataStream: DataStream[String] = getDataStreamSource(logs, className, env, args)

        val sinkTopic: String = "dwd_ifttt_user_action"
//    val sinkTopic = "flink_test"
    //
    val sink: FlinkKafkaProducer[String] = KafkaUtil.getKafkaProduce(sinkTopic, isNewKafka = true)
    
    dataStream.addSink(sink).name("sink2dwd_ifttt_user_action")
//        dataStream.print()
    //
    env.execute(className)
  }


  /**
   * 获取基础数据流
   *
   * @param source   数据源
   * @param env      env环境
   * @param consumer 消费者id
   * @return 合并数据流
   */
  def getDataStreamSourceBase(source: String, env: StreamExecutionEnvironment, consumer: String, args: Array[String]): DataStream[IftttUserInfo] = {
    var dataStream: DataStream[IftttUserInfo] = null
    var sourceTopic: String = ""

    val consumerId: String = source + "_" + consumer
    val isNewKafka: Boolean = true
    source match {
      case IFTTT_LOG.LOG_SENSORS =>

        // sensors
        val sensorsStream: DataStream[CommonLog] = StreamExecutionEnvUtil.getKafkaSourceCommonLog(env, SINK_TOPIC.SINK_SENSORS, className, args)
        dataStream = sensorsStream.map(sensors => JSON.parseObject(sensors.getLog, classOf[SensorsLog]))
          .name("sensorsSource")
          .process(new SensorsUserInfoFillFunction)
          .name("sensorsStream")

      case IFTTT_LOG.LOG_ACTION =>
        sourceTopic = "dwd_action"
        val actionConsumer: DataStream[CommonLog] = StreamExecutionEnvUtil.getKafkaSourceCommonLog(env, SINK_TOPIC.SINK_ACTION, className, args)
        dataStream = actionConsumer.map(action => JSON.parseObject(action.getLog, classOf[Pojos.ActionLog]))
          .name("actionSource")
          .process(new ActionUserInfoFillFunction)
          .name("actionStream")

      case IFTTT_LOG.LOG_PV =>

        // pv
        val pvStream: DataStream[CommonLog] = StreamExecutionEnvUtil.getKafkaSourceCommonLog(env, SINK_TOPIC.SINK_PV, className, args)
        dataStream = pvStream.map(pv => JSON.parseObject(pv.getLog, classOf[PvLog]))
          .name("pvSource")
          .process(new PvUserInfoFillFunction)
          .name("pvStream")

      case IFTTT_LOG.LOG_RESSHOW | IFTTT_LOG.LOG_RESCLICK =>

        // sensors
        val sensorsStream: DataStream[CommonLog] = StreamExecutionEnvUtil.getKafkaSourceCommonLog(env, SINK_TOPIC.SINK_CTR, className, args)
        dataStream = sensorsStream.map(res =>
          JSON.parseObject(res.getLog, classOf[ResShowLog])
        )
          .name("resshowSource")
          .process(new ResShowUserInfoFillFunction())
          .name("resshowStream")


      case IFTTT_LOG.LOG_NGINX =>
        sourceTopic = "dwd_nginx"
        val nginxConsumer: FlinkKafkaConsumer[NginxLog] = new KafkaUtil[NginxLog].getKafkaConsumer(sourceTopic, consumerId, classOf[NginxLog], isNewKafka)
        nginxConsumer.setStartFromLatest()
        val nginxStream: DataStream[NginxLog] = env.addSource(nginxConsumer)
          .name("nginxSource")
        dataStream = nginxStream
          .process(new UserAccessExtractProcessFunction)
          .keyBy(info => (info.uid, info.showId))
          .process(new UserAccessKeyedProcessFunction)
          .name("nginxStream")
    }

    dataStream
  }

  /**
   * 获取基础数据流
   *
   * @param source   数据源
   * @param env      env环境
   * @param consumer 消费者id
   * @return 合并数据流
   */
  def getDataStreamSource(source: String, consumer: String, env: StreamExecutionEnvironment, args: Array[String]): DataStream[String] = {
    source.split(",")
      .map(source => {
        getDataStreamSourceBase(source, env, consumer, args)
      })
      .reduce((a, b) => a.union(b))
      .map(info => {
        val gson: Gson = new Gson()
        gson.toJson(info)
      })
  }

}
