package com.hhz.ifttt.pattern


import com.hhz.ifttt.functions.{DynamicKeySelector,RuleBroadcastAndFilterProcessFunction, RuleFillFunction, RulePatternKeyedProcessFunction}
import com.hhz.ifttt.pojo.Pojos
import com.hhz.ifttt.pojo.Pojos.{Rule, RuleEvent}
import com.hhz.ifttt.utils.{KafkaUtil, StreamExecutionEnvUtil}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.datastream.BroadcastStream
import org.apache.flink.streaming.api.scala.{AsyncDataStream, DataStream, OutputTag, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.slf4j.{Logger, LoggerFactory}

/**
 * @program ifttt
 * @description: 规则匹配处理器
 * @author: zyh
 * @create: 2021/09/16 16:04
 **/
object RuleTest {

  private val className: String = this.getClass.getSimpleName.replaceAll("\\$", "")
  private val logger: Logger = LoggerFactory.getLogger(className)
  val userProfileOutputTag: OutputTag[String] = OutputTag[String]("user_profile")

  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvUtil.getStreamExecutionEnv(className, isLocal = true)
    // 事件流
    val sourceTopic: String = "dwd_ifttt_user_action"
    //    val sourceTopic: String = "flink_test"
    val consumer: FlinkKafkaConsumer[String] = KafkaUtil.getKafkaSimpleConsumer(sourceTopic, className, isNewKafka = true)
    consumer.setStartFromEarliest()

    val eventStream: DataStream[String] = env.addSource(consumer)
      .name(sourceTopic + "_source")
    // 规则流

    val ruleConsumer: FlinkKafkaConsumer[String] = KafkaUtil.getKafkaSimpleConsumer("ifttt_rule_test", className, isNewKafka = true)
    ruleConsumer.setStartFromEarliest()

    val ruleStream: DataStream[Rule] =
     env.addSource(ruleConsumer)
        .process(new RuleFillFunction)
        .name("ruleStream")
        .setParallelism(1)
    ruleStream.print("rule").name("ruleStream print")

    // 广播流
    val ruleBroadcastStream: BroadcastStream[Rule] = ruleStream.broadcast(Pojos.ruleDesc)
    // 广播流 规则表达式
    val ruleExpressionBroadcastStream: BroadcastStream[Rule] = ruleStream.broadcast(Pojos.expressionMapDesc)



    // 规则预过滤 + rule关联   需要使用 非规则流 connect(规则流)
    val ruleBroadcastAndFilterProcessStream: DataStream[RuleEvent] =
      eventStream
        .connect(ruleBroadcastStream)
        .process(new RuleBroadcastAndFilterProcessFunction)
        .name("ruleBroadcastAndFilter")

    // 匹配逻辑处理
    val rulePatternKeyedProcessStream: DataStream[String] =
      ruleBroadcastAndFilterProcessStream
        .keyBy(_.key)
        .connect(ruleExpressionBroadcastStream)
        .process(new RulePatternKeyedProcessFunction)
        .name("RulePatternKeyedProcessFunction")



    /** ----------------------------------------- flink sink  -------------------------------------------------- */
    //    ruleBroadcastAndFilterProcessStream.print("用户数据：")
    //    rulePatternKeyedProcessStream.print("已匹配：----->>>")
    //     中间数据落地kudu用于查错
//    ruleBroadcastAndFilterProcessStream
//      .map(new Gson().toJson(_))
//      .addSink(KafkaUtil.getKafkaProduce("dwd_ifttt_user_pattern_data", isNewKafka = true))
//      .name("sink:dwd_ifttt_user_pattern_data")
//
//
//    //     用户画像处理器 侧输出流
//    val userProfileStream: DataStream[String] = rulePatternKeyedProcessStream.getSideOutput(userProfileOutputTag)
//    val userProfileSink: FlinkKafkaProducer[String] = KafkaUtil.getKafkaProduce("dwd_ifttt_user_profile_data", isNewKafka = true)
//    userProfileStream
//      .addSink(userProfileSink)
//      .name("sink:dwd_ifttt_user_profile_data")
//
//    //     结果输出流
//    val sink: FlinkKafkaProducer[String] = KafkaUtil.getKafkaProduce("flink_cep_pattern_result", isNewKafka = true)
//    rulePatternKeyedProcessStream
//      .addSink(sink)
//      .name("sink:flink_cep_pattern_result")

    env.execute(className)


  }


  /**
   * 获取数据流
   * @param env        env环境
   * @param consumerId 消费者id
   */
  def getSourceStream(env: StreamExecutionEnvironment, consumerId: String, sourceTopic: String): DataStream[String] = {
    val consumer: FlinkKafkaConsumer[String] = KafkaUtil.getKafkaSimpleConsumer(sourceTopic, consumerId, isNewKafka = true)
    val stream: DataStream[String] = env.addSource(consumer)
      .name(sourceTopic + "_source")
    stream
  }
}
