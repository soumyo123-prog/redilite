package com.redish.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ConnectionHandler {
  public void handleAccept(Selector selector, SelectionKey key) throws IOException {
    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

    // Accepting the new client connection and do configurations.
    SocketChannel clientSocketChannel = serverSocketChannel.accept();
    clientSocketChannel.configureBlocking(false);

    ClientConnection clientConnection = new ClientConnection();

    // Register the new client connection for read events.
    clientSocketChannel.register(selector, SelectionKey.OP_READ, clientConnection);
  }

  public void handleRead(SelectionKey key) throws IOException {
    // Since this would be a client connection as it is ready to read.
    SocketChannel clientSocketChannel = (SocketChannel) key.channel();
    ClientConnection clientConnection = (ClientConnection) key.attachment();

    // Allocate a buffer and read data from channel (pipe) into the buffer (bucket).
    ByteBuffer buffer = clientConnection.getReadBuffer();

    int bytesRead = clientSocketChannel.read(buffer);
    if (bytesRead == -1) {
      // Connection was closed by client.
      this.closeConnection(key);
      return;
    }

    clientConnection.prepareResponse("+PONG\r\n");

    // Set the interest of the current key to OP_WRITE (in order to write the PONG
    // response).
    key.interestOps(SelectionKey.OP_WRITE);

    // Next time selector.select() runs:
    // - It will check if channel is ready for writing
    // - If yes, key.isWritable() will return true
    // - Your handleWrite method will be called
  }

  public void handleWrite(SelectionKey key) throws IOException {
    SocketChannel clientChannel = (SocketChannel) key.channel();
    ClientConnection connection = (ClientConnection) key.attachment();
    ByteBuffer buffer = connection.getWriteBuffer();

    clientChannel.write(buffer);
    if (!buffer.hasRemaining()) {
      buffer.clear();
      // Again, set the interest of this key to READABLE so that its ready for the
      // next cycle.
      key.interestOps(SelectionKey.OP_READ);
    }
  }

  public void closeConnection(SelectionKey key) {
    try {
      key.channel().close();
    } catch (IOException e) {
      System.err.println("Error closing connection: " + e.getMessage());
    } finally {
      key.cancel();
    }
  }

}
