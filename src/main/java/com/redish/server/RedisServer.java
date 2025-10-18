package com.redish.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.redish.thread.RedisThread;

public class RedisServer {
  private int port = 6379;

  public RedisServer() {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      while (true) {
        Socket clientSocket = serverSocket.accept();
        RedisThread redisThread = new RedisThread(clientSocket);
        Thread thread = new Thread(redisThread);
        thread.start();
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
