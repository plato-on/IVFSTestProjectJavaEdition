package main.java.com.platon.exception;

public class FileWriterEntityIsNullException extends NullPointerException {
    public FileWriterEntityIsNullException () {
        System.out.println("FileWriterEntity is null");
        printStackTrace();
    }
}
