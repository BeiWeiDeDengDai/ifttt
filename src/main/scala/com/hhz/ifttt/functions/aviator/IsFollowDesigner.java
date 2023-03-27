package com.hhz.ifttt.functions.aviator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;
import java.util.Scanner;

/**
 * @program ifttt
 * @description:
 * @author: zhangyinghao
 * @create: 2022/10/19 17:02
 **/
public class IsFollowDesigner extends AbstractFunction {

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject str) {
        String stringValue = FunctionUtils.getStringValue(str, env);
        if (stringValue.equals("8427009")) {
            return AviatorBoolean.TRUE;
        }else {
            return AviatorBoolean.FALSE;
        }
    }

    @Override
    public String getName() {
        return "isFollowDesigner";
    }


    public static void main(String[] args) throws InterruptedException {
//        AviatorEvaluator.addFunction(new IsFollowDesigner());
        new Thread(()->{
            while (true) {
                Scanner sc = new Scanner(System.in);
                String next = sc.next();
                try {
                if (next.startsWith("add")){

                        Class<?> isFollowDesigner = Class.forName("com.hhz.ifttt.functions.aviator.IsFollowDesigner");
                        Object o = isFollowDesigner.newInstance();
                        AviatorEvaluator.addFunction((AbstractFunction)o);

                }
                if (next.startsWith("remove")) {
                    AviatorEvaluator.removeFunction("isFollowDesigner");
                }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }

            }
        }).start();


        while (true) {
            try{
                Thread.sleep(2000);
                Expression compile = AviatorEvaluator.compile("isFollowDesigner(uid)" );
                JSONObject json = new JSONObject();
                json.put("uid", "8427009");
                System.out.println(compile.execute(json));
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }
}
