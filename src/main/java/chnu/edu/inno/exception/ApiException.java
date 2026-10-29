package chnu.edu.inno.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


// Базовий виняток застосунку
@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}