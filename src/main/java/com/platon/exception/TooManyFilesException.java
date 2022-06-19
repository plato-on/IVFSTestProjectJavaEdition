package main.java.com.platon.exception;

public class TooManyFilesException extends RuntimeException {
    public TooManyFilesException(String message) {
        super(message);
    }
}
