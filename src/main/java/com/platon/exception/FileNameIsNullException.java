package main.java.com.platon.exception;

public class FileNameIsNullException extends NullPointerException {
    public FileNameIsNullException() {
        System.out.println("Filename is null");
        printStackTrace();
    }
}
