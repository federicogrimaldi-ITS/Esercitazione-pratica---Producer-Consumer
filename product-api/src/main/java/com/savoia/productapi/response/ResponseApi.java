package com.savoia.productapi.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Getter
public class ResponseApi<T> {

    private LocalDateTime timestamp;
    private HttpStatus httpStatus;
    private String error;
    private String message;
    private Map<String, String> errors;
    private T data;
}
