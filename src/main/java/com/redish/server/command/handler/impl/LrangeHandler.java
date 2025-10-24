package com.redish.server.command.handler.impl;

import java.util.List;

import com.redish.server.ClientConnection;
import com.redish.server.command.handler.CommandHandler;
import com.redish.server.resp.RespParser;
import com.redish.server.store.ListStore;

public class LrangeHandler implements CommandHandler {
  private final ListStore listStore;

  public LrangeHandler(ListStore listStore) {
    this.listStore = listStore;
  }

  @Override
  public String handle(List<String> args, ClientConnection connection) {
    if (args.size() != 3) {
      return RespParser.convertToErrorString("Wrong number of arguments used for 'lrange' command. Expected: 3");
    }

    String listKey = args.get(0);
    Integer startIdx = null;
    Integer endIdx = null;

    try {
      startIdx = Integer.parseInt(args.get(1));
      endIdx = Integer.parseInt(args.get(2));
    } catch (NumberFormatException e) {
      return RespParser
          .convertToErrorString("Invalid arguments used for 'lrange' command. Expected: <key> <start_idx> <end_idx>");
    }

    return RespParser.convertToArray(listStore.lrange(listKey, startIdx, endIdx));
  }

  @Override
  public String name() {
    return "LRANGE";
  }

}
