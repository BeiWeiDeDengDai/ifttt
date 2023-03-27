package com.hhz.ifttt.functions

import com.hhz.ifttt.pojo.Pojos.RuleEvent
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.java.functions.KeySelector


/**
 * @program ifttt 
 * @description: 动态选择key
 * @author: zyh 
 * @create: 2021/09/28 18:05 
 **/
@Deprecated
class DynamicKeySelector extends KeySelector[RuleEvent, String]{
  override def getKey(value: RuleEvent): String = {
    var key: StringBuilder = new StringBuilder()
    val uid: String = value.uid
    key.append(uid)
      .append(value.rule.groupId)

    // 如果动态字段不为空则进行拼接动态字段
    if (StringUtils.isNotBlank(value.rule.dynamicFieldName)) {
      key.append(",")
      value.rule.dynamicFieldName.split(",").foreach(i => {

        key.append(i).append(",")
      })

      key.substring(0, key.length-1)
    }
    key.toString()
  }

}
