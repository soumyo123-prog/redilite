package com.redish.server.command.implementation;

import java.util.List;

import com.redish.server.ClientConnection;
import com.redish.server.command.CommandHandler;
import com.redish.server.resp.RespParser;

public class EchoHandler implements CommandHandler {

  @Override
  public String handle(List<String> args, ClientConnection connection) {
    if (args.isEmpty()) {
      return RespParser.convertToBulkString("");
    }
    String arg = args.get(0); // ECHO the first argument only.
    return RespParser.convertToBulkString(arg);
  }

  @Override
  public String name() {
    return "ECHO";
  }

}
