package com.iasdlc.domain.exception;

/**
 * Códigos de error del contrato de API. La capa web los traduce a un estado HTTP.
 */
public enum ErrorCode {
    VALIDATION_ERROR,
    COMMENT_REQUIRED,
    UNAUTHORIZED,
    INVALID_CREDENTIALS,
    NOT_GATE_APPROVER,
    NOT_FOUND,
    EMAIL_ALREADY_REGISTERED,
    NO_STORIES,
    AGENT_NOT_AVAILABLE,
    PHASE_AWAITING_GATE,
    PHASE_ALREADY_APPROVED,
    EXECUTION_ALREADY_DECIDED,
    DUPLICATE_NAME,
    /** No está en el contrato: el usuario ya tiene ese rol en el squad. */
    DUPLICATE_MEMBER
}
