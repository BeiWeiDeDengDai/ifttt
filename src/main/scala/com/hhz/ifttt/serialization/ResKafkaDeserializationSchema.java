package com.hhz.ifttt.serialization;

import com.hhz.ifttt.pojo.CommonLog;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * @program realtime
 * @description:
 * @author: zhangyinghao
 * @create: 2022/07/25 15:52
 **/
public class ResKafkaDeserializationSchema implements KafkaDeserializationSchema<CommonLog> {



    @Override
    public boolean isEndOfStream(CommonLog nextElement) {
        return false;
    }

    @Override
    public CommonLog deserialize(ConsumerRecord<byte[], byte[]> record) throws Exception {
        return new CommonLog(record.partition(), record.offset(), new String(record.value()));
    }


    @Override
    public TypeInformation<CommonLog> getProducedType() {
        return TypeInformation.of(CommonLog.class);
    }
}
