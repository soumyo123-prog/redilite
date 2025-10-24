package com.redish.server.store;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ListStore {
  Map<String, LinkedList<String>> store = new HashMap<>();

  public int rpush(String listKey, String value) {
    LinkedList<String> list = store.get(listKey);
    if (null == list) {
      list = new LinkedList<>();
    }

    list.addLast(value);
    store.put(listKey, list);

    return list.size();
  }
}
