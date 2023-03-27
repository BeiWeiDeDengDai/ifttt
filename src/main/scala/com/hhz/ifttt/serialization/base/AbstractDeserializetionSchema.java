package com.hhz.ifttt.serialization.base;

import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AbstractDeserializetionSchema<T> {

    private static Injection<GenericRecord, byte[]> recordInjection = null;

    private  Class clazz(Class clazz){
        String className = clazz.getName();
        Class cl = clazz;

        switch(className)
        {
            case "int" :
                cl = Integer.class;
                break;
            case "org.apache.avro.Schema":
                cl = null;
                break;

        }
        return cl;
    }

    private  String method(String filed){
        String[] arr = filed.split("_");
        StringBuilder sb = new StringBuilder();
        sb.append("set");
        for(String a:arr){
            sb.append(a.substring(0, 1).toUpperCase() + a.substring(1));
        }
        return sb.toString();
    }

    public T genericRecord(Class clazz, byte[] bytes)throws Exception{


        Object obj  = clazz.newInstance();
        if(recordInjection==null){

            Method getClassSchema = clazz.getMethod("getClassSchema");
            Schema schema = (Schema)getClassSchema.invoke(null);
            recordInjection = GenericAvroCodecs.toBinary(schema);
        }

        GenericRecord genericRecord = recordInjection.invert(bytes).get();

        Field[] fields = clazz.getFields();
        for (Field field:fields){
            String filedName = field.getName();
            String setMethod = method(filedName);
            Class filedT = clazz(field.getType());
            if(filedT!=null){
                Method method = clazz.getMethod(setMethod,filedT);
                method.invoke(obj,genericRecord.get(filedName));
            }

        }
        return (T)obj;
    }


}
