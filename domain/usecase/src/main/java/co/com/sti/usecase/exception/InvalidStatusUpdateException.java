package co.com.sti.usecase.exception;

public class InvalidStatusUpdateException extends RuntimeException {
    public InvalidStatusUpdateException(String message) {
        super(message);
    }
}
