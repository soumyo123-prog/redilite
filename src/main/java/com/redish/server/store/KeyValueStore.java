package com.redish.server.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class KeyValueStore {
  private final Map<String, String> store = new HashMap<>();

  public void set(String key, String value) {
    store.put(key, value);
  }

  public Optional<String> get(String key) {
    return Optional.ofNullable(store.get(key));
  }
}
