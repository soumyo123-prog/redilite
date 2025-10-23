package com.redish.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.redish.server.command.CommandRegistry;
import com.redish.server.command.SimpleCommandProcessor;
import com.redish.server.command.handler.impl.EchoHandler;
import com.redish.server.command.handler.impl.GetHandler;
import com.redish.server.command.handler.impl.PingHandler;
import com.redish.server.command.handler.impl.SetHandler;
import com.redish.server.store.KeyValueStore;

public class RedisServer implements AutoCloseable {
  private final String host;
  private final int port;
  private final Selector selector;
  private final ServerSocketChannel serverSocketChannel;
  private volatile boolean isRunning;
  private final ConnectionHandler connectionHandler;
  private final KeyValueStore keyValueStore;

  public RedisServer(String host, int port) throws IOException {
    this.host = host;
    this.port = port;

    // Selector monitors multiple channels at once.
    this.selector = Selector.open();

    // Channels are like pipes of data which replace traditional data streams.
    // They can be configured as non-blocking, hence, they are essential here.
    // ServerSocketChannel: This is our listener pipe, whose only job is to listen
    // for new connections.
    this.serverSocketChannel = ServerSocketChannel.open();

    // The core key-value store for our redis server.
    KeyValueStore keyValueStore = new KeyValueStore();
    this.keyValueStore = keyValueStore;

    CommandRegistry registry = new CommandRegistry();
    registry.register(new EchoHandler());
    registry.register(new GetHandler(keyValueStore));
    registry.register(new SetHandler(keyValueStore));

    // Ping handler is currently a fallback for all the unsupported commands.
    this.connectionHandler = new ConnectionHandler(new SimpleCommandProcessor(registry, new PingHandler()));
  }

  public void start() throws IOException {
    this.serverSocketChannel.bind(new InetSocketAddress(this.host, this.port));
    this.serverSocketChannel.configureBlocking(false);

    // Telling the selector to watch the serverSocketChannel and notify about new
    // connections.
    this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);

    this.isRunning = true;

    runEventLoop();
  }

  private void runEventLoop() throws IOException {
    while (isRunning) {
      // Only blocking call in the server. Waits until atleast one of the registered
      // channels are ready for an event. In our case, it waits until the registered
      // serverSocketChannel is ready for accepting new connections.
      int readyChannels = this.selector.select(100);

      if (readyChannels > 0) {
        // Event is ready, we take the keys (tickets) for all the ready channels. In
        // this case only one registered channel is ready till now. A 'SelectionKey'
        // tells us two things: a) Which channel is ready and b) What is is ready for?
        Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();

        while (iterator.hasNext()) {
          SelectionKey key = iterator.next();
          // Removal of a processed key is essential to avoid repeated processing of same
          // event.
          iterator.remove();

          if (!key.isValid())
            continue;

          try {
            if (key.isAcceptable()) {
              this.connectionHandler.handleAccept(this.selector, key);
            } else if (key.isReadable()) {
              this.connectionHandler.handleRead(key);
            } else if (key.isWritable()) {
              this.connectionHandler.handleWrite(key);
            }
          } catch (IOException e) {
            this.connectionHandler.closeConnection(key);
          }
        }
      }

      this.keyValueStore.runActiveExpiration();
    }
  }

  @Override
  public void close() {
    this.isRunning = false;
    try {
      if (this.selector != null && this.selector.isOpen()) {
        selector.close();
      }
      if (this.serverSocketChannel != null && this.serverSocketChannel.isOpen()) {
        this.serverSocketChannel.close();
      }
    } catch (IOException e) {
      System.err.println("Error while closing server: " + e.getMessage());
    }
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
