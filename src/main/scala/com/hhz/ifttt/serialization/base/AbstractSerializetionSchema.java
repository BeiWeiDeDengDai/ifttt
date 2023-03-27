package com.hhz.ifttt.serialization.base;

import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AbstractSerializetionSchema<T> {

    private static Injection<GenericRecord, byte[]> recordInjection = null;
    private static Schema schema = null;
    private static Field[] fields = null;


    public byte[] serializeObj(Class clazz,T t){
        GenericData.Record avroRecord = null;
        try {
            if (schema == null || fields == null || recordInjection == null) {
                Method getClassSchema = clazz.getMethod("getClassSchema");
                schema = (Schema) getClassSchema.invoke(null);
                fields = clazz.getFields();
                recordInjection = GenericAvroCodecs.toBinary(schema);
            }

            avroRecord = new GenericData.Record(schema);

            for (Field field : fields) {
                String filedName = field.getName();
                if(!"SCHEMA$".equals(filedName)){
                    String method = getMethod(filedName);
                    Method getMethod = clazz.getMethod(method);
                    avroRecord.put(filedName, getMethod.invoke(t));
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        byte[] bytes = recordInjection.apply(avroRecord);
        return bytes;
    }


    private  String getMethod(String filed){
        String[] arr = filed.split("_");
        StringBuilder sb = new StringBuilder();
        sb.append("get");
        for(String a:arr){
            sb.append(a.substring(0, 1).toUpperCase() + a.substring(1));
        }
        return sb.toString();
    }


}
