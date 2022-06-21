package main.java.com.platon.exception;

public class WriteBufferIsEmptyOrNullException extends NullPointerException {
    public WriteBufferIsEmptyOrNullException() {
        System.out.println("Buffer to write from is null or empty ");
        printStackTrace();
    }
}
