package com.iasdlc.domain.service;

import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;
import com.iasdlc.domain.model.Phase;
import com.iasdlc.domain.model.Squad;

import java.util.UUID;

/**
 * Regla de quién aprueba un gate: un integrante del squad (el del proyecto de la
 * iniciativa) con el rol aprobador de la fase, sea titular o suplente.
 */
public final class GatePolicy {

    private GatePolicy() {
    }

    public static boolean isApprover(Squad squad, Phase phase, UUID userId) {
        return squad.hasRole(userId, phase.approverRole());
    }

    public static void ensureApprover(Squad squad, Phase phase, UUID userId) {
        if (!isApprover(squad, phase, userId)) {
            throw new DomainException(ErrorCode.NOT_GATE_APPROVER,
                    "Solo un integrante del squad con el rol " + phase.approverRole()
                            + " puede decidir el gate de esta fase");
        }
    }
}
