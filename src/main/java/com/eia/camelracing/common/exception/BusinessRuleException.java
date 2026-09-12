package com.eia.camelracing.common.exception;

/**
 * Se lanza cuando una operación entra en conflicto con datos que ya existen:
 * apodos o nombres duplicados, un competidor que ya milita en otro equipo,
 * una posición de carrera ya ocupada, un registro con historial oficial que
 * no puede eliminarse, etc. El GlobalExceptionHandler la traduce a un
 * 409 Conflict.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
