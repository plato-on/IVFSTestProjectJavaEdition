package main.java.com.platon.exception;

public class FileStatusException extends RuntimeException {
    public FileStatusException() {
        System.out.println("Failed to change file status to valid one.");
        printStackTrace();
    }
}
