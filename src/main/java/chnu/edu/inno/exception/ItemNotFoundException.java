package chnu.edu.inno.exception;

import org.springframework.http.HttpStatus;

public class ItemNotFoundException extends ApiException {
    public ItemNotFoundException(String id) {
        super(HttpStatus.NOT_FOUND, "Item with id '" + id + "' was not found");
    }
}