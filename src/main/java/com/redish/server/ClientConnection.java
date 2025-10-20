package com.redish.server;

import java.nio.ByteBuffer;

public class ClientConnection {
  private final ByteBuffer readBuffer;
  private final ByteBuffer writeBuffer;

  public ClientConnection() {
    this.readBuffer = ByteBuffer.allocate(1024);
    this.writeBuffer = ByteBuffer.allocate(1024);
  }

  public ByteBuffer getReadBuffer() {
    return this.readBuffer;
  }

  public ByteBuffer getWriteBuffer() {
    return this.writeBuffer;
  }

  public void prepareResponse(String response) {
    writeBuffer.clear();
    writeBuffer.put(response.getBytes());
    // Initially, the buffer is in read mode and we will use the writeBuffer for
    // writing.
    writeBuffer.flip();
  }
}
