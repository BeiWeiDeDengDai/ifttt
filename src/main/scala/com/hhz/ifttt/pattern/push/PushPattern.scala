package com.hhz.ifttt.pattern.push

import java.util
import java.util.Date

import com.alibaba.fastjson.{JSON, JSONObject}
import com.google.gson.Gson
import com.hhz.ifttt.logs.BaseLogMerge.className
import com.hhz.ifttt.pojo.{CommonLog, Pojos}
import com.hhz.ifttt.pojo.Constants.SINK_TOPIC
import com.hhz.ifttt.pojo.Pojos.{PatternResult, SensorsLog}
import com.hhz.ifttt.utils.{KafkaUtil, StreamExecutionEnvUtil}
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.{KeyedProcessFunction, ProcessFunction}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer
import org.apache.flink.util.Collector
import org.apache.kafka.clients.producer.KafkaProducer

/**
 * @program ifttt 
 * @description:  push 场景活动
 * @author: zhangyinghao 
 * @create: 2022/12/06 14:53 
 **/
object PushPattern {
  private val className: String = this.getClass.getSimpleName.replaceAll("\\$", "")

  //  personal_push_
  //  scene_push_active_note_
  //  scene_push_active_circle_
  //  scene_push_active_article_
  //  scene_push_active_h5_
  //  scene_push_active_integral_
  //  scene_push_collected_new_
  //  scene_push_browsed_new_


  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvUtil.getStreamExecutionEnv(className, isOSS = true)

    val sensorsStream: DataStream[CommonLog] = StreamExecutionEnvUtil.getKafkaSourceCommonLog(env, SINK_TOPIC.SINK_SENSORS, className, args)
    val resStream: DataStream[String] = sensorsStream
      .map(sensors => JSON.parseObject(sensors.getLog, classOf[SensorsLog]))
      .filter(push => {
        var flag: Boolean = false
        if (push.uid != null && push.uid != "" && push.uid != "0") {
          if (push.category.equals("push") && push.event_type.equals("click")) {
            val params: JSONObject = JSON.parseObject(push.params)
            val pushId: String = params.getString("push_id")
            if (pushId != null) {
              if (pushId.startsWith("personal_push_") || pushId.startsWith("scene_push_active_note_") || pushId.startsWith("scene_push_active_circle_")
                || pushId.startsWith("scene_push_active_article_") || pushId.startsWith("scene_push_active_h5_") || pushId.startsWith("scene_push_active_integral_")
                || pushId.startsWith("scene_push_collected_new_") || pushId.startsWith("scene_push_browsed_new_")) {
                flag = true
              }
            }
          }
        }
        flag
      })
      .keyBy(_.uid)
      .process(new pushProcessFunction)
    resStream.print()

    val producer: FlinkKafkaProducer[String] = KafkaUtil.getKafkaProduce("flink_cep_pattern_result")
    resStream.addSink(producer)
    env.execute()


  }

  class pushProcessFunction extends KeyedProcessFunction[String, Pojos.SensorsLog, String] {

    var valueState: ValueState[String] = _

    override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[String, SensorsLog, String]#OnTimerContext, out: Collector[String]): Unit = {
      val map: util.HashMap[String, String] = new util.HashMap[String, String](1)
      val data: String = valueState.value()
      map.put("begin", data)
      valueState.update(null)
      out.collect(new Gson().toJson(PatternResult("ifttt_task_606",ctx.getCurrentKey, new Date().toString, map, "")))
    }

    override def open(parameters: Configuration): Unit = {
      valueState = getRuntimeContext.getState(new ValueStateDescriptor[String]("valueState", TypeInformation.of(classOf[String])))
    }

    override def processElement(sensors: SensorsLog, ctx: KeyedProcessFunction[String, Pojos.SensorsLog, String]#Context, out: Collector[String]): Unit = {

      if (valueState.value() == null) {
        valueState.update(new Gson().toJson(sensors))
        // 首次 注册时间差
        val time: Long = System.currentTimeMillis() + (10 * 1000L)
        ctx.timerService().registerProcessingTimeTimer(time)
//        println(s"注册触发器: ${new Date(time)}")
      }
    }
  }

}

