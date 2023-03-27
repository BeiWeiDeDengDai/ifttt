package com.hhz.ifttt.pattern.v3

import java.util.concurrent.TimeUnit

import com.google.gson.Gson
import com.hhz.ifttt.functions.RuleFillFunction
import com.hhz.ifttt.functions.v3.{AsyncStarRocksRequestFunction, PreFiltrationProcessFunction}
import com.hhz.ifttt.pojo.{Constants, Pojos}
import com.hhz.ifttt.pojo.Pojos.{Rule, RuleEvent, ruleDesc}
import com.hhz.ifttt.utils.{KafkaUtil, StreamExecutionEnvUtil}
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.{AsyncDataStream, DataStream, KeyedStream, OutputTag, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.datastream.BroadcastStream
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer
import sun.security.provider.MD5

/**
 * @program ifttt 
 * @description:
 *              1. 数据预过滤
 *              2. 维度实时填充
 *              3. 数据下发
 *              flink run -d -ynm UserEventPatternHandler -ytm 2048 \
 *              -yjm 1024 \
 *              -p 1 \
 *              -ys 1 \
 *              -c com.hhz.ifttt.pattern.v3.UserEventPatternHandler /Users/zhangyinghao/bigdata/ifttt/target/ifttt-1.0-SNAPSHOT-jar-with-dependencies.jar
 * @author: zhangyinghao
 * @create: 2022/10/18 13:53 
 **/
object UserEventFilterAndFIll {

  private val className: String = this.getClass.getSimpleName.replaceAll("\\$", "")




  def main(args: Array[String]): Unit = {
    // 参数配置
    val tool: ParameterTool = ParameterTool.fromArgs(args)
    val size: Int = tool.get("size", "5000").toInt
    val redisSize: Int = tool.get("redisSize", "1").toInt
    val filterSize: Int = tool.get("filterSize","1").toInt
    val oss: Boolean = tool.get("isOss", "false").toBoolean
    val env: StreamExecutionEnvironment = StreamExecutionEnvUtil.getStreamExecutionEnv(className, checkPointTime = 600 * 2, isOSS = oss)
    val parallelism: Int = env.getParallelism

    env.setParallelism(1)
    // 事件流获取 dwd_ifttt_user_action
    val ctrLogStream: DataStream[String] = StreamExecutionEnvUtil.getKafkaSource(env, "ifttt_base_log_merge_v3", className, args)


    // 规则流获取
    val ruleStream: DataStream[Rule] = getRuleDataStream(env, "ifttt_rule_test", className, args).setParallelism(1)
    // 广播流
    val ruleBroadcastStream: BroadcastStream[Rule] = ruleStream.broadcast(ruleDesc)


    // 规则流
    val filterStream: DataStream[RuleEvent] =
      ctrLogStream
        .connect(ruleBroadcastStream)
        .process(new PreFiltrationProcessFunction)
        .setParallelism(parallelism * filterSize)
        .name("pre filtration")

    // 分组
    val shuffleStream: DataStream[RuleEvent] = filterStream
      .keyBy(_.uid.toString.hashCode % parallelism)





    // 维度匹配
    val dimStream: DataStream[RuleEvent] = AsyncDataStream
      .unorderedWait(shuffleStream, new AsyncStarRocksRequestFunction, 60, TimeUnit.SECONDS, size)
      .setParallelism(parallelism * redisSize)
      .name("async-redis")





    /**==================== producer 记录 ==================== */
    val producer: FlinkKafkaProducer[String] = KafkaUtil.getKafkaProduce(Constants.SINK_TOPIC.COMPLETE_STREAM)
     val out: FlinkKafkaProducer[String] = KafkaUtil.getKafkaProduce(Constants.SINK_TOPIC.OUT_LOG)
//
    filterStream.getSideOutput(Pojos.iftttOutLog).map(new Gson().toJson(_))
      .addSink(out).name("ifttt_out_log")
//    // 事件类型 完成A
    dimStream.map(new Gson().toJson(_)).addSink(producer).name("CompleteA")
    dimStream.map(_.data).print("结果")
    env.execute()
  }


  /**
   * 获取规则流
   *
   * @param env
   * @param ruleTopic
   * @param args
   * @return
   */
  def getRuleDataStream(env: StreamExecutionEnvironment, ruleTopic: String, consumerId: String, args: Array[String]): DataStream[Rule] = {
    // todo 上线后修改
    args(1) = "-1"
    StreamExecutionEnvUtil.getKafkaSource(env, ruleTopic, consumerId, args)
      .process(new RuleFillFunction)
      .name("ruleStream")
      .setParallelism(1)
  }
}
