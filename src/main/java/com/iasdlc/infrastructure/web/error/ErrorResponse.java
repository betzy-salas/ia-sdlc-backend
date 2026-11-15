package com.iasdlc.infrastructure.web.error;

import java.util.List;

/** Forma de error del contrato: { code, message, details }. */
public record ErrorResponse(String code, String message, List<String> details) {
}
