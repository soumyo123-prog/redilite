package com.redish.server.command.handler.impl;

import java.util.List;

import com.redish.server.ClientConnection;
import com.redish.server.command.handler.CommandHandler;
import com.redish.server.resp.RespParser;
import com.redish.server.store.KeyValueStore;

public class SetHandler implements CommandHandler {
  private final KeyValueStore keyValueStore;

  public SetHandler(KeyValueStore keyValueStore) {
    this.keyValueStore = keyValueStore;
  }

  @Override
  public String handle(List<String> args, ClientConnection connection) {
    if (args.isEmpty() || args.size() != 2) {
      return RespParser.convertToErrorString("Wrong number of arguments for 'set' command. Expected: 2.");
    }

    String key = args.get(0);
    String value = args.get(1);

    keyValueStore.set(key, value);

    return RespParser.convertToSimpleString("OK");
  }

  @Override
  public String name() {
    return "SET";
  }
}
