package com.redish.server.store;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ListStore {
  Map<String, LinkedList<String>> store = new HashMap<>();

  public int rpush(String listKey, String... values) {
    LinkedList<String> list = store.get(listKey);
    if (null == list) {
      list = new LinkedList<>();
    }

    for (String value : values) {
      list.addLast(value);
    }

    store.put(listKey, list);
    return list.size();
  }

  public List<String> lrange(String listKey, int startIdx, int endIdx) {
    LinkedList<String> list = store.get(listKey);
    if (list == null) {
      return List.of();
    }

    endIdx = Math.min(endIdx, list.size() - 1);

    if (startIdx >= list.size() || startIdx > endIdx) {
      return List.of();
    }

    return new LinkedList<>(list.subList(startIdx, endIdx + 1));
  }
}
