package com.savoia.productapi.payload.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    public static <T> ResponseEntity<ResponseApi<T>> buildResponse(HttpStatus status, T data) {
        ResponseApi<T> response = ResponseApi.<T>builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .data(data)
                .build();
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    public static <T> ResponseEntity<ResponseApi<T>> buildResponse(HttpStatus status, T data, String message) {
        ResponseApi<T> response = ResponseApi.<T>builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    public static <T> ResponseEntity<ResponseApi<T>> buildResponse(HttpStatus status, String message) {
        ResponseApi<T> response = ResponseApi.<T>builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .message(message)
                .build();
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }
}
