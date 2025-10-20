package com.redish;

import java.io.IOException;

import com.redish.server.RedisServer;

public class Main {
  public static void main(String[] args) {
    try (RedisServer server = new RedisServer("localhost", 6379)) {
      server.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}