package com.hhz.ifttt.serialization;

import com.hhz.ifttt.pojo.UserAction;
import com.hhz.ifttt.serialization.base.AbstractSerializetionSchema;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;

/**
 * @program realtime
 * @description: 用户行为，神策，资源位合并 avro序列化
 * @author: zhangyinghao
 * @create: 2020/05/25 11:01
 **/
public class UserActionKeyedSerializationSchema extends AbstractSerializetionSchema<UserAction>
                  implements KeyedSerializationSchema<UserAction> {

    @Override
    public byte[] serializeKey(UserAction element) {
        return element.toString().getBytes();
    }
    @Override
    public byte[] serializeValue(UserAction element) {
        return serializeObj(UserAction.class, element);
    }
    @Override
    public String getTargetTopic(UserAction element) {
        return null;
    }
}
