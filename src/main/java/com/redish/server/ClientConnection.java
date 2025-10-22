package com.redish.server;

import java.nio.ByteBuffer;

public class ClientConnection {
  private final ByteBuffer readBuffer;
  private final ByteBuffer writeBuffer;

  public ClientConnection() {
    // Both buffers start in write mode:
    // position=0, limit=capacity=1024
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
    // Clear any existing content and reset to write mode.
    writeBuffer.clear();
    // Write the response bytes.
    writeBuffer.put(response.getBytes());
    // Flip to read mode so the data can be written to the channel.
    // Writing into channel => reading from buffer.
    writeBuffer.flip();
  }
}
