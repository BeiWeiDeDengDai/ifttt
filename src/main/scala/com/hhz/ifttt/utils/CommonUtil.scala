package com.hhz.ifttt.utils

import com.alibaba.fastjson.JSONObject
import org.apache.commons.lang3.{ObjectUtils, StringUtils}

/**
 * @program ifttt 
 * @description: 公共类
 * @author: zyh 
 * @create: 2021/09/13 16:33 
 **/
object CommonUtil {

  /**
   * 从params中获取id
   * @param params
   * @return
   */
  def getObjId(params: String): String = {
    try {
      val paramsMap: Map[String, String] = strToMap(params)
      val objId: String = paramsMap.getOrElse("obj_id", null)
      val articleId: String = paramsMap.getOrElse("article_id", null)
      val activity_id: String = paramsMap.getOrElse("activity_id", null)
      val ideabook_id: String = paramsMap.getOrElse("ideabook_id", null)
      val photo_id: String = paramsMap.getOrElse("photo_id", null)
      val answer_id: String = paramsMap.getOrElse("answer_id", null)
      val question_id: String = paramsMap.getOrElse("question_id", null)
      val blank_id: String = paramsMap.getOrElse("blank_id", null)

      // 榜单  wiki
      val wikiID: String = paramsMap.getOrElse("wikiID", null)
      val wiki_id: String = paramsMap.getOrElse("wiki_id", null)
      val ranking_id: String = paramsMap.getOrElse("ranking_id", null)


      ObjectUtils.firstNonNull(objId, articleId, activity_id, ideabook_id, photo_id, answer_id, question_id, blank_id, wikiID, ranking_id, wiki_id,"")

    }catch {
      case e: Exception => {
        ""
      }
    }
  }


  /**
   * 获取作者id
   * @param objId
   * @return
   */
  def getAuthorId(objId: String): String = {
    var author_uid: String = ""
    try {
      if (StringUtils.isNotBlank(objId) && objId.length == 16) {
        val str: String = objId.substring(10)
        author_uid = ConvFunction.transRadix(str, 36, 10)
      }
      author_uid
    } catch {
      case ex: Exception => {
        ex.printStackTrace()
        author_uid
      }
    }
  }

  /**
   * params 字段解析
   *
   * @param str
   * @return
   */
  def getJSON(str: String): String = {
    val nObject: JSONObject = new JSONObject()
    if (str != null) {
      str.split(",").map(info => {
        val items: Array[String] = info.split("=")
        try {
          nObject.put(items(0), items(1))
        } catch {
          case e: Exception => null
        }
      })
    }
    nObject.toString
  }


  /**
   * 字符串转map
   *
   * @param str
   * @return
   */
  def strToMap(str: String): Map[String, String] = {
    val map: Map[String, String] = str.split(",").map(info => {
      val items: Array[String] = info.split("=")
      try {
        (items(0), items(1))
      } catch {
        case e: Exception => null
      }
    }).filter(_ != null).toMap
    map
  }


  /**
   * nginx param数据转换
   * @param str
   * @return
   */
  def paramsToMap(str: String): Map[String, String] = {
    val map: Map[String, String] = str.split("&").map(info => {
      val items: Array[String] = info.split("=")
      try {
        (items(0), items(1))
      } catch {
        case e: Exception => null
      }
    }).filter(_ != null).toMap
    map
  }

}
