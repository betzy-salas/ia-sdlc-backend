package com.iasdlc.application.port.in;

import com.iasdlc.application.view.ExecutionView;
import com.iasdlc.domain.model.ExecutionStatus;
import com.iasdlc.domain.model.PhaseCode;

import java.util.List;
import java.util.UUID;

public interface ExecutionUseCase {
    /** Ejecuta el agente de la fase sobre las historias del proyecto (síncrono). */
    ExecutionView runPhase(UUID projectId, PhaseCode phaseCode, UUID launchedBy);
    /** Lista ejecuciones; los filtros nulos no se aplican. */
    List<ExecutionView> listExecutions(UUID projectId, ExecutionStatus status);
    ExecutionView getExecution(UUID executionId);
}
