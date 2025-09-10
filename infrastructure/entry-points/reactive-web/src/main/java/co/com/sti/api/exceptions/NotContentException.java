package co.com.sti.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NO_CONTENT)
public class NotContentException extends RuntimeException{
    public NotContentException(String message) {
        super(message);
    }
}
