package com.iasdlc.domain.exception;

/**
 * Excepción de negocio. Lleva el código de error del contrato y un mensaje legible en español.
 */
public class DomainException extends RuntimeException {

    private final ErrorCode code;

    public DomainException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }

    public static DomainException notFound(String what) {
        return new DomainException(ErrorCode.NOT_FOUND, "No existe " + what);
    }
}
