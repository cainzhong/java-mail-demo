/*
 * (C) Copyright Hewlett-Packard Company, LP -  All Rights Reserved.
 */
package com.java.mail;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONObject;

/**
 * @author zhontao
 *
 */
public class JSONUtil {

  /**
   * Convert JSONObject to Map<String, Object>.
   * 
   * @param jsonObject
   * @return a Map&lt;String, Object&gt;
   */
  public static Map<String, Object> convertJsonToMap(JSONObject jsonObject) {
    HashMap<String, Object> map = new HashMap<String, Object>();
    if (jsonObject != null && !jsonObject.isEmpty()) {
      Iterator<?> it = jsonObject.keys();
      while (it.hasNext()) {
        String key = String.valueOf(it.next());
        Object value = jsonObject.get(key);
        map.put(key, value);
      }
    }
    return map;
  }
}
