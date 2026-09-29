package com.savoia.productapi.exception;

import com.savoia.productapi.payload.response.ResponseApi;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@ControllerAdvice
public class ExceptionManagement {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> resourceNotFoundExceptionManagement(ResourceNotFoundException ex){
        HttpStatus status = HttpStatus.NOT_FOUND;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> missingServletRequestParameterExceptionManagement(MissingServletRequestParameterException ex){
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> methodArgumentTypeMismatchExceptionManagement(MethodArgumentTypeMismatchException ex){
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> methodArgumentTypeMismatchExceptionManagement(NoResourceFoundException ex){
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<?> constraintViolationExceptionManagement(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        // Last node of the property path = the parameter/field name ("registrationUser.arg0" → "arg0").
        Map<String, String> errors = violations.stream()
                .collect(Collectors.toMap(
                        v -> {
                            String path = v.getPropertyPath().toString();
                            return path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                        },
                        ConstraintViolation::getMessage,
                        (first, second) -> first,
                        LinkedHashMap::new));

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message("Alcuni campi non sono validi.")
                .errors(errors)
                .build();
        return new ResponseEntity<>(response, status);
    }


    /*@ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> badCredentialsExceptionManagement(BadCredentialsException ex){
        HttpStatus status = HttpStatus.FORBIDDEN;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, status);
    }


    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> disabledExceptionManagement(DisabledException ex){
        HttpStatus status = HttpStatus.FORBIDDEN;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, status);
    }*/


  /*  @ExceptionHandler(GenericException.class)
    public ResponseEntity<?> genericExceptionManagement(GenericException ex){
        HttpStatus status = HttpStatus.FORBIDDEN;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, status);
    }*/



    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<?> handleArgumentNotValidValue(MethodArgumentNotValidException ex) {
        BindingResult bindingResults = ex.getBindingResult();
        Map<String, String> errors = bindingResults
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        org.springframework.validation.FieldError::getField,
                        e -> e.getDefaultMessage() == null ? "valore non valido" : e.getDefaultMessage(),
                        (first, second) -> first,
                        LinkedHashMap::new));

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message("Alcuni campi non sono validi.")
                .errors(errors)
                .build();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalArgumentExceptionManagement(IllegalArgumentException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseApi<?> response = ResponseApi.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status)
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, status);
    }
}
