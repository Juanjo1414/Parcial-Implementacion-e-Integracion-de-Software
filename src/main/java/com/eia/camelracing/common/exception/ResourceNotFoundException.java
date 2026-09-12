package com.eia.camelracing.common.exception;

/**
 * Se lanza cuando se busca una entidad por identificador y no existe.
 * El GlobalExceptionHandler la traduce a un 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
