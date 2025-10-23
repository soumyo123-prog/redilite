package com.redish.server.command.handler.impl;

import java.util.List;

import com.redish.server.ClientConnection;
import com.redish.server.command.handler.CommandHandler;

public class PingHandler implements CommandHandler {

  @Override
  public String handle(List<String> args, ClientConnection connection) {
    return "+PONG\r\n";
  }

  @Override
  public String name() {
    return "PING";
  }

}
