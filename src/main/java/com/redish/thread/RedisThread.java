package com.redish.thread;

import java.io.IOException;
import java.net.Socket;

public class RedisThread implements Runnable {
  private Socket clientSocket;

  public RedisThread(Socket socket) {
    this.clientSocket = socket;
  }

  @Override
  public void run() {
    try {
      while (true) {
        this.sendPongResponse();
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } finally {
      this.closeClientSocket();
    }
  }

  private void sendPongResponse() throws IOException {
    byte[] input = new byte[1024];
    clientSocket.getInputStream().read(input);

    String output = "+PONG\r\n";
    clientSocket.getOutputStream().write(output.getBytes());
    clientSocket.getOutputStream().flush();
  }

  private void closeClientSocket() {
    try {
      if (clientSocket != null) {
        clientSocket.close();
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
