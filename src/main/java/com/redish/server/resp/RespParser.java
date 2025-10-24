package com.redish.server.resp;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RespParser {
  private static final char ARRAY_PREFIX = '*';
  private static final char BULK_STRING_PREFIX = '$';

  public static String convertToBulkString(String data) {
    if (data == null) {
      return "$-1\r\n";
    }

    StringBuilder sb = new StringBuilder();

    sb.append('$');
    sb.append(data.getBytes().length);
    sb.append("\r\n");
    sb.append(data);
    sb.append("\r\n");

    return sb.toString();
  }

  public static String convertToSimpleString(String data) {
    return String.format("+%s\r\n", data);
  }

  public static String convertToErrorString(String message) {
    return String.format("-ERR %s\r\n", message);
  }

  public static String convertToInteger(int num) {
    return String.format(":%d\r\n", num);
  }

  public static List<String> parseCommand(BufferedReader br) throws IOException {
    String header = br.readLine();

    if (null == header || header.isEmpty()) {
      throw new IOException("Empty input, expected RESP array header.");
    }

    if (header.charAt(0) != ARRAY_PREFIX) {
      throw new IOException("Protocol error: Expected array prefix '*', got '" + header.charAt(0) + "'");
    }

    int numElements;
    try {
      numElements = Integer.parseInt(header.substring(1));
    } catch (NumberFormatException e) {
      throw new IOException("Protocol error: Malformed array length.", e);
    }

    // Case of null array.
    if (numElements == -1) {
      return null;
    }

    List<String> commandParts = new ArrayList<>();
    for (int i = 0; i < numElements; i++) {
      commandParts.add(parseBulkString(br));
    }

    return commandParts;
  }

  private static String parseBulkString(BufferedReader br) throws IOException {
    String header = br.readLine();
    if (header == null || header.isEmpty()) {
      throw new IOException("Empty input, expected RESP bulk string header.");
    }

    if (header.charAt(0) != BULK_STRING_PREFIX) {
      throw new IOException("Protocol error: Expected bulk string prefix '$', got '" + header.charAt(0) + "'");
    }

    int length;
    try {
      length = Integer.parseInt(header.substring(1));
    } catch (NumberFormatException e) {
      throw new IOException("Protocol error: Malformed bulk string length.", e);
    }

    // Handle null bulk string (e.g., "$-1\r\n")
    if (length == -1) {
      return null;
    }

    // Handle empty bulk string (e.g., "$0\r\n\r\n")
    if (length == 0) {
      String empty = br.readLine(); // Consume the empty line
      if (empty == null || !empty.isEmpty()) {
        throw new IOException("Protocol error: Expected empty line for zero-length bulk string.");
      }
      return "";
    }

    // Read the data part. We read exactly 'length' characters. This is the standard
    // approach to read an exact number of characters from a stream in Java.
    char[] buffer = new char[length];
    int totalCharsRead = 0;
    while (totalCharsRead < length) {
      int charsRead = br.read(buffer, totalCharsRead, length - totalCharsRead);
      if (charsRead == -1) {
        // End of stream before we read all expected data.
        // It means the stream ended before we got all the length characters we were
        // promised. This is a protocol error.
        throw new IOException("Protocol error: Incomplete bulk string data. Expected " + length + " bytes.");
      }
      totalCharsRead += charsRead;
    }

    // After the data, we must consume the final trailing CRLF
    // read() consumes one character at a time.
    if (br.read() != '\r' || br.read() != '\n') {
      throw new IOException("Protocol error: Missing trailing CRLF after bulk string data.");
    }

    return new String(buffer);
  }
}
