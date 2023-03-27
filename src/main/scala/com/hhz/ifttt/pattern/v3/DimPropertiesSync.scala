package com.hhz.ifttt.pattern.v3

import com.alibaba.fastjson.JSON
import com.hhz.ifttt.functions.v3.DimDataBraodcastTable
import com.hhz.ifttt.pojo.Pojos
import com.hhz.ifttt.utils.StreamExecutionEnvUtil
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.slf4j.{Logger, LoggerFactory}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.datastream.BroadcastStream
/**
 * @program ifttt
 * @description:
 *     维度数据同步 / 2 redis
 * @author: zhangyinghao
 * @create: 2022/10/25 16:11
 **/
object DimPropertiesSync {

  private val className: String = this.getClass.getSimpleName.replaceAll("\\$", "")
  private val logger: Logger = LoggerFactory.getLogger(className)


  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvUtil.getStreamExecutionEnv(className)
    env.setParallelism(1)
    val dimStream: DataStream[String] = StreamExecutionEnvUtil.getKafkaSource(env, "ods_hhz_bigdata_binlog", className, args)


    val tableProStream: DataStream[String] = StreamExecutionEnvUtil.getKafkaSource(env, "test", className, args)
    val tableBroadcast: BroadcastStream[String] = tableProStream.broadcast(Pojos.tableProdesc)


    // 维度数据
    dimStream
        .connect(tableBroadcast)
      .process(new DimDataBraodcastTable)
      .print()


    env.execute()
  }

}
