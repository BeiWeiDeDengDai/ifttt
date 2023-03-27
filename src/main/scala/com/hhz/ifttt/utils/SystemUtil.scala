package com.hhz.ifttt.utils

/**
 * @program realtimex 
 * @description: ${TODO} 
 * @author: zhangyinghao 
 * @create: 2022/09/06 10:35 
 **/
object SystemUtil {


  def isWindows(): Boolean = {
    val osName: String = getOsName()

    osName != null && osName.startsWith("Windows");
  }

  /**
   * 判断操作系统是否是 MacOS
   *
   * @return true：操作系统是 MacOS
   *         false：其它操作系统
   */
  def isMacOs() : Boolean ={
    val osName: String = getOsName();

    osName != null && osName.startsWith("Mac")
  }

  def isLinux(): Boolean = {
    val osName: String = getOsName()

    (osName != null && osName.startsWith("Linux")) || (!isWindows() && !isMacOs());
  }



  def getOsName(): String ={
    System.getProperty("os.name")
  }
}
