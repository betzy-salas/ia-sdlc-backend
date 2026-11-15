package com.iasdlc.application.port.in;

import com.iasdlc.application.view.ExecutionView;
import com.iasdlc.domain.model.Decision;

import java.util.List;
import java.util.UUID;

public interface ApprovalUseCase {
    /** Ejecuciones pendientes de aprobación que el usuario puede decidir. */
    List<ExecutionView> pendingApprovals(UUID userId);
    ExecutionView decide(UUID executionId, UUID userId, Decision decision, String comment);
}
