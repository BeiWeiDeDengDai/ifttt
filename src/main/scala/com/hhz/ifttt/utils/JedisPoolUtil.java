package com.hhz.ifttt.utils;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolUtil {

    private static volatile JedisPool jedisPool = null;

    private JedisPoolUtil() {
    }

    public static JedisPool getJedisPoolInstance(String host, int port) {

        if (null == jedisPool) {
            synchronized (JedisPoolUtil.class) {
                if (null == jedisPool) {
                    JedisPoolConfig config = new JedisPoolConfig();
                    config.setMaxTotal(40);
                    config.setMaxIdle(20);
                    config.setTestOnBorrow(true);//获取链接检查有效性
                    config.setTestOnReturn(false);//返回验证
                    config.setBlockWhenExhausted(true);//链接好近是否阻塞
                    config.setTestOnCreate(true);//部署时 为True;
                    config.setMaxWaitMillis(1000 * 60 * 1);//最大等待毫秒数

//                    jedisPool = new JedisPool(config, host, port, 3000,passwd);
                    jedisPool = new JedisPool(config, host, port, 3000);

                }
            }
        }

        return jedisPool;

    }

    public static void release(JedisPool jedispool, Jedis jedis) {
        if (null != jedis) {
            jedis.close();
        }
    }
}