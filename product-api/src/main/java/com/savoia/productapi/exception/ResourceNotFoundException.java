package com.savoia.productapi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception representing a resource not found error (HTTP 404 - Not Found).
 * <p>
 * Indicates that a requested resource cannot be found using the provided
 * search criteria.
 */
@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException{

    /** Name of the resource that was not found. */
    private final String resourceName;

    /** Field used to search for the resource. */
    private final String fieldName;

    /** Field value used in the search. */
    private final Object fieldValue;

    /**
     * Creates a new resource not found exception with details about the missing resource.
     *
     * @param resourceName the name of the resource that was not found
     * @param fieldName the field used to search for the resource
     * @param fieldValue the value of the field that was used in the search
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s non trovato con %s: %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
