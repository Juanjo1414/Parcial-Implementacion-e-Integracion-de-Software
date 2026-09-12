package com.eia.camelracing.common.exception;

/**
 * Se lanza cuando una acción no es válida dado el estado actual de una
 * entidad: una carrera fuera de la ventana de inscripción, un resultado
 * cargado sobre una carrera que no está en curso, una inscripción que se
 * intenta aprobar sin estar pendiente, etc. El GlobalExceptionHandler la
 * traduce a un 400 Bad Request.
 */
public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
