package com.redish.server.command;

import java.util.HashMap;
import java.util.Map;

import com.redish.server.command.handler.CommandHandler;

import java.util.Locale;

public class CommandRegistry {
  private final Map<String, CommandHandler> handlers = new HashMap<>();

  public void register(CommandHandler handler) {
    handlers.put(handler.name().toUpperCase(Locale.ROOT), handler);
  }

  public CommandHandler get(String name) {
    return handlers.get(name.toUpperCase(Locale.ROOT));
  }
}