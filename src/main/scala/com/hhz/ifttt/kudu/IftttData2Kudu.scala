//package com.hhz.ifttt.kudu
//
//import java.util
//
//import com.google.common.collect.Lists
//import com.hhz.ifttt.utils.StreamExecutionEnvUtil
//import org.apache.flink.connectors.kudu.connector.writer.{AbstractSingleOperationMapper, KuduWriterConfig, RowOperationMapper}
//import org.apache.flink.connectors.kudu.connector.{ColumnSchemasFactory, CreateTableOptionsFactory, KuduTableInfo}
//import org.apache.flink.connectors.kudu.streaming.KuduSink
//import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
//import org.apache.flink.types.{Row, RowKind}
//import org.apache.kudu.client.CreateTableOptions
//import org.apache.kudu.{ColumnSchema, Type}
//import org.apache.flink.api.scala._
///**
// * @program ifttt
// * @description: ${TODO}
// * @author: zhangyinghao
// * @create: 2022/12/06 13:34
// **/
//object IftttData2Kudu {
//
//
//  private val className: String = this.getClass.getSimpleName.replaceAll("\\$", "")
//
//
//  def main(args: Array[String]): Unit = {
//
//    val env: StreamExecutionEnvironment = StreamExecutionEnvUtil.getStreamExecutionEnv(className, isOSS = true)
//    val value: DataStream[String] = StreamExecutionEnvUtil.getKafkaSource(env, "test", className, args)
//
//    value.print()
//
//    val tableName: String = "aliyun_flink"
//    val writerConfig: KuduWriterConfig = KuduWriterConfig.Builder
//      .setMasters("172.17.0.33:7051,172.17.0.34:7051,172.17.0.32:7051")
//      .setOperationTimeout(10 *1000L)
//      .build()
//    val sink: KuduSink[Row] = new KuduSink[Row](
//      writerConfig,
//      KuduTableInfo.forTable(tableName),
//      new RowOperationMapper(Array( "day","uid", "name"), AbstractSingleOperationMapper.KuduOperation.UPSERT)
//    )
//    value
//      .map(info =>{
//        val strings: Array[String] = info.split(",")
//        val row: Row = new Row(RowKind.INSERT, 3)
//        row.setField(0, 20221205)
//        row.setField(1, strings(0))
//        row.setField(2, strings(1))
//        row
//      })
//      .addSink(sink)
//
////    KuduTableInfo
////      .forTable(tableName)
////      .createTableIfNotExists(new ColumnSchemasFactory {
////        override def getColumnSchemas: util.List[ColumnSchema] =
////          Lists.newArrayList(getColumnSchema("name"),
////            getColumnSchema("uid"),
////            getColumnSchema("day", Type.INT32, isKey = true))
////      }, new CreateTableOptionsFactory {
////        override def getCreateTableOptions: CreateTableOptions = {
////          new CreateTableOptions
////        }
////      })
//
//    env.execute()
//  }
//
//  // kudu.default.aliyun_flink
//
//
//  def getColumnSchema(field: String, tp: Type = Type.STRING, isKey: Boolean = false): ColumnSchema = {
//    new ColumnSchema.ColumnSchemaBuilder(field, tp)
//      .key(isKey)
//      .build()
//  }
//
//}
