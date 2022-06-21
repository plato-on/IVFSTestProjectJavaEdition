package main.java.com.platon.exception;

import java.io.IOException;

public class BufferTooLargeException extends IOException {
    public BufferTooLargeException() {
        System.out.println("Amount of buffer to read/write is too large. Make necessary length = 2147483639 bytes as maximum size.");
        printStackTrace();
    }
}