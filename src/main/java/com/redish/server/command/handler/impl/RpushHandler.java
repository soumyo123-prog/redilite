package com.redish.server.command.handler.impl;

import java.util.List;

import com.redish.server.ClientConnection;
import com.redish.server.command.handler.CommandHandler;
import com.redish.server.resp.RespParser;
import com.redish.server.store.ListStore;

public class RpushHandler implements CommandHandler {
  private final ListStore listStore;

  public RpushHandler(ListStore listStore) {
    this.listStore = listStore;
  }

  @Override
  public String handle(List<String> args, ClientConnection connection) {
    if (args.size() != 2) {
      return RespParser.convertToErrorString("Wrong number of arguments for 'rpush' command. Expected: 2");
    }

    String listKey = args.get(0);
    String value = args.get(1);

    int size = this.listStore.rpush(listKey, value);
    return RespParser.convertToInteger(size);
  }

  @Override
  public String name() {
    return "RPUSH";
  }

}
