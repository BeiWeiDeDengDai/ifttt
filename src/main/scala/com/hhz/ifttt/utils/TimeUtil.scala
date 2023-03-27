package com.hhz.ifttt.utils

import java.text.SimpleDateFormat
import java.util.{Date, Locale}

/**
 * @program TimeUtil
 * @description: 时间处理器
 * @author: zhangyinghao 
 * @create: 2020/03/31 16:55 
 **/
object TimeUtil {


  /**
   * 解析时间
   *
   * @param time
   * @param sdf
   */
  def parseTime(time: String, sdf: String = "yyyyMMdd") = {
    val sd: SimpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss", Locale.US)
    val date: Date = sd.parse(time.toString)
    val format: SimpleDateFormat = new SimpleDateFormat(sdf)
    format.format(date)
  }


  /**
   * 解析时间为时间戳
   * @param time 时间
   * @param sdf 时间格式
   */
  def parseDateToTimestamp(time: String, sdf: String = "yyyyMMdd") = {
    val format: SimpleDateFormat = new SimpleDateFormat(sdf)
    format.parse(time).getTime
  }
  /**
   * 时间检查
   * @param df
   * @param day
   * @return
   */
  def checkDay(df: SimpleDateFormat, day: Int): Boolean = {
    import java.util.Calendar
    val calendar: Calendar = Calendar.getInstance
    calendar.setTimeInMillis(System.currentTimeMillis())
    calendar.add(Calendar.DATE,-1)
    val time: Int = df.format(calendar.getTime).toInt
    if (day >= time) true else false
  }

  /**
   * 时间转换
   * @param oldSdf
   * @param newSdf
   */
  def transTime(time: String, oldSdf: String = "yyyy-MM-dd HH:mm:ss", newSdf: String = "yyyy-MM-dd HH:mm") = {
    val sd: SimpleDateFormat = new SimpleDateFormat(oldSdf)
    val date: Date = sd.parse(time)
    val format: SimpleDateFormat = new SimpleDateFormat(newSdf)
    format.format(date)
  }


  /**
   * 时间戳转时间
   * @param ts
   * @return
   */
  def timestampToDate(ts: Long, sdf: String = "yyyy-MM-dd HH"): String ={
    val sd: SimpleDateFormat = new SimpleDateFormat(sdf)
    val str: String = sd.format(new Date(ts * 1000))
    str
  }

  def main(args: Array[String]): Unit = {
    println(timestampToDate(1620699549))
  }



}
