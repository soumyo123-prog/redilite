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
    String listKey = args.get(0);
    List<String> values = args.subList(1, args.size());

    int size = this.listStore.rpush(listKey, values.toArray(new String[0]));
    return RespParser.convertToInteger(size);
  }

  @Override
  public String name() {
    return "RPUSH";
  }

}
