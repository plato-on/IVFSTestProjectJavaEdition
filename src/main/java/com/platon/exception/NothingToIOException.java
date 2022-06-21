package main.java.com.platon.exception;

public class NothingToIOException extends Throwable {
    public NothingToIOException() {
        System.out.println("You set 0 amount of bytes to be read/written ");
    }
}
