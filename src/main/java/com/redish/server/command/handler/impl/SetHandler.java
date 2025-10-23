package com.redish.server.command.handler.impl;

import java.util.List;

import com.redish.server.ClientConnection;
import com.redish.server.command.handler.CommandHandler;
import com.redish.server.resp.RespParser;
import com.redish.server.store.KeyValueStore;

public class SetHandler implements CommandHandler {
  private static final String EX = "EX";
  private static final String PX = "PX";

  private final KeyValueStore keyValueStore;

  public SetHandler(KeyValueStore keyValueStore) {
    this.keyValueStore = keyValueStore;
  }

  @Override
  public String handle(List<String> args, ClientConnection connection) {
    if (args.isEmpty() || !(args.size() == 2 || args.size() == 4)) {
      return RespParser.convertToErrorString("Wrong number of arguments for 'set' command. Expected: 2.");
    }

    String key = args.get(0);
    String value = args.get(1);
    Long expiryTimeStamp = null;

    if (args.size() == 4) {
      if (!args.get(2).equalsIgnoreCase(EX) && !args.get(2).equalsIgnoreCase(PX)) {
        return RespParser.convertToErrorString("Wrong expiry option used. Expected: PX or EX");
      }

      try {
        Long duration = Long.parseLong(args.get(3));
        Long mills = args.get(2).equalsIgnoreCase(PX) ? duration : duration * 1000;
        expiryTimeStamp = System.currentTimeMillis() + mills;
      } catch (NumberFormatException e) {
        return RespParser.convertToErrorString("Invalid expiry duration used. Expected: Number");
      }
    }

    keyValueStore.set(key, value, expiryTimeStamp);

    return RespParser.convertToSimpleString("OK");
  }

  @Override
  public String name() {
    return "SET";
  }
}
