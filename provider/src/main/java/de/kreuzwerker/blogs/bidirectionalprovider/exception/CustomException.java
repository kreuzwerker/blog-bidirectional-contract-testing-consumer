package de.kreuzwerker.blogs.bidirectionalprovider.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomException extends RuntimeException {
    Integer code;
    String message;

    public CustomException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

}
