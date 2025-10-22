package com.redish.server.command;

import java.util.List;
import com.redish.server.ClientConnection;

public interface CommandProcessor {
  /**
   * Process a parsed RESP command. command.get(0) is the command name.
   * Returns a RESP-formatted response (e.g. "+PONG\r\n" or "-ERR ...\r\n").
   */
  String process(List<String> command, ClientConnection connection);
}
