package com.redish.server.command;

import java.util.List;
import com.redish.server.ClientConnection;

/** Single-command handler: handle a single command name. */
public interface CommandHandler {
  String handle(List<String> args, ClientConnection connection);

  String name();
}