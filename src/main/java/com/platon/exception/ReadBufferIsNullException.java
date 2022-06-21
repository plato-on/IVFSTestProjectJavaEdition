package main.java.com.platon.exception;

public class ReadBufferIsNullException extends NullPointerException {
    public ReadBufferIsNullException() {
        System.out.println("Readbuffer is null ");
        printStackTrace();
    }
}
