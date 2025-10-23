package com.redish.server.command;

import java.util.List;

import com.redish.server.ClientConnection;
import com.redish.server.command.handler.CommandHandler;

public class SimpleCommandProcessor implements CommandProcessor {
  private final CommandRegistry registry;
  private final CommandHandler defaultHandler;

  public SimpleCommandProcessor(CommandRegistry registry) {
    this(registry, null);
  }

  public SimpleCommandProcessor(CommandRegistry registry, CommandHandler defaultHandler) {
    this.registry = registry;
    this.defaultHandler = defaultHandler;
  }

  @Override
  public String process(List<String> command, ClientConnection connection) {
    if (command == null || command.isEmpty()) {
      return "-ERR empty command\r\n";
    }
    CommandHandler handler = registry.get(command.get(0));
    if (handler == null) {
      if (defaultHandler != null) {
        return defaultHandler.handle(command.subList(1, command.size()), connection);
      }
      return "-ERR unknown command\r\n";
    }
    return handler.handle(command.subList(1, command.size()), connection);
  }
}
