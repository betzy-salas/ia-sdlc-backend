package com.iasdlc.application.port.out;

import com.iasdlc.domain.model.User;

/** Emite el token de acceso de un usuario autenticado. */
public interface TokenIssuer {
    String issue(User user);
}
