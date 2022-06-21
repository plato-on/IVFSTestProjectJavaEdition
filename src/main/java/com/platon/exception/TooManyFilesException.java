package main.java.com.platon.exception;

public class TooManyFilesException extends RuntimeException {
    public TooManyFilesException() {
        System.out.println("System handles only 10 physical files.");
        printStackTrace();
    }
}
