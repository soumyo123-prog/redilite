package com.redish.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class RedisServer {
  private int port = 6379;
  private Selector selector;

  public RedisServer() {
    /**
     * Following is the event-loop based implementation of non-blocking TCP server
     * in JAVA.
     */

    try {
      // Selector monitors multiple channels at once.
      this.selector = Selector.open();

      // Channels are like pipes of data which replace traditional data streams.
      // They can be configured as non-blocking, hence, they are essential here.
      // ServerSocketChannel: This is our listener pipe, whose only job is to listen
      // for new connections.
      ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
      serverSocketChannel.bind(new InetSocketAddress("localhost", port));
      serverSocketChannel.configureBlocking(false);

      // Telling the selector to watch the serverSocketChannel and notify about new
      // connections.
      serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);

      // The "event-loop".
      while (true) {
        // Only blocking call in the server. Waits until atleast one of the registered
        // channels are ready for an event. In our case, it waits until the registered
        // serverSocketChannel is ready for accepting new connections.
        this.selector.select();

        // Event is ready, we take the keys (tickets) for all the ready channels. In
        // this case only one registered channel is ready till now. A 'SelectionKey'
        // tells us two things: a) Which channel is ready and b) What is is ready for?
        Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();

        while (iterator.hasNext()) {
          SelectionKey key = iterator.next();

          if (key.isAcceptable()) {
            this.handleAccept(key);
          } else if (key.isReadable()) {
            this.handleRead(key);
          }

          // Removal of a processed key is essential to avoid repeated processing of same
          // event.
          iterator.remove();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    /**
     * Following is the thread implementation of the redis server, which is good for
     * less number of concurrent users as it is based on one thread per connection.
     * Consumes more resources and does not scale-up so well.
     * 
     * try (ServerSocket serverSocket = new ServerSocket(port)) {
     * while (true) {
     * Socket clientSocket = serverSocket.accept();
     * RedisThread redisThread = new RedisThread(clientSocket);
     * Thread thread = new Thread(redisThread);
     * thread.start();
     * }
     * } catch (IOException e) {
     * System.out.println("IOException: " + e.getMessage());
     * }
     */
  }

  private void handleAccept(SelectionKey key) throws IOException {
    // Since, at this stage, only the serverSocketChannel was registered for accept
    // events.
    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

    // Accepting the new client connection and do configurations.
    SocketChannel client = serverSocketChannel.accept();
    client.configureBlocking(false);

    // Register the new client connection for read events.
    client.register(this.selector, SelectionKey.OP_READ);
  }

  private void handleRead(SelectionKey key) throws IOException {
    // Since this would be a client connection as it is ready to read.
    SocketChannel clientSocketChannel = (SocketChannel) key.channel();

    // Allocate a buffer and read data from channel (pipe) into the buffer (bucket).
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    int bytesRead = clientSocketChannel.read(buffer);

    if (bytesRead == -1) {
      // Connection was closed by client.
      clientSocketChannel.close();
      key.cancel();
      return;
    }

    buffer.clear();
    buffer.put("+PONG\r\n".getBytes());

    // Switch the buffer to write-mode (this is essential when we actually want to
    // process the client message).
    buffer.flip();
    clientSocketChannel.write(buffer);
  }
}
