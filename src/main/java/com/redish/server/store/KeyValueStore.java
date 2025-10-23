package com.redish.server.store;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class KeyValueStore {
  private final Map<String, String> store = new HashMap<>();
  private final Map<String, Long> expires = new HashMap<>();

  public void set(String key, String value, Long expiryTimeStamp) {
    store.put(key, value);
    if (null != expiryTimeStamp) {
      expires.put(key, expiryTimeStamp);
    }
  }

  public Optional<String> get(String key) {
    if (expires.containsKey(key) && expires.get(key) <= System.currentTimeMillis()) {
      store.remove(key);
      expires.remove(key);
    }
    return Optional.ofNullable(store.get(key));
  }

  public void runActiveExpiration() {
    // How many keys to check per cycle.
    // This number is a trade-off: higher is more accurate but "blocks" for longer.
    // 20 is a good default.
    final int KEYS_TO_CHECK_PER_CYCLE = 20;
    int keysChecked = 0;
    long now = System.currentTimeMillis();

    Iterator<Map.Entry<String, Long>> iterator = expires.entrySet().iterator();

    while (iterator.hasNext() && keysChecked < KEYS_TO_CHECK_PER_CYCLE) {
      Map.Entry<String, Long> entry = iterator.next();
      keysChecked++;

      if (entry.getValue() <= now) {
        store.remove(entry.getKey());
        iterator.remove();
      }
    }
  }
}
