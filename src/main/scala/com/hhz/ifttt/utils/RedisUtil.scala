package com.hhz.ifttt.utils

/**
 * @program realtime 
 * @description: 获取Redis地址
 * @author: zhangyinghao 
 * @create: 2021/06/28 14:21 
 **/
object RedisUtil {


  /**
   * 获取 redis地址
   * @param confPath 配置文件路径
   * @param tag tag
   */
  def getRedisHostAndPort(confPath: String, tag: String): (String, Int, String) = {
    val ini: IniReader = new IniReader(confPath)
    val host: String = ini.getValue(tag, "host").split(",")(0)
    val port: Integer = Integer.valueOf(ini.getValue(tag, "port").split(",")(0))
    val passwd: String = ini.getValue(tag, "passwd")
    (host, port, passwd)
  }




  def getRedis(): (String, Int, String) = {
//    ("r-2zeyxh2w62nxpq7q0h.redis.rds.aliyuncs.com",6379,"hZrdS15zong")
//     ("7819.write.redis.haohaozhu.me", 7819, "hZrdS15")RulePatternKeyedProcessFunction
//    ("hadoop10", 6379, "hZrdS15")
    ("localhost", 6379, "hZrdS15")
  }
}
