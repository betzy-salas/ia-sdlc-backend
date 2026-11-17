package com.iasdlc.domain;

import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;
import com.iasdlc.domain.model.Decision;
import com.iasdlc.domain.model.Execution;
import com.iasdlc.domain.model.ExecutionStatus;
import com.iasdlc.domain.model.Initiative;
import com.iasdlc.domain.model.PhaseCode;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.iasdlc.domain.DomainFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExecutionTest {

    private final Initiative initiative = DomainFixtures.initiativeWithStories(2);
    private final UUID approver = UUID.randomUUID();

    @Test
    void startsRunningAndCompletesAsPendingApproval() {
        Execution execution = Execution.start(initiative.id(), PhaseCode.REQUIREMENTS,
                DomainFixtures.executableAgent(), "0.1.0", UUID.randomUUID(), NOW);
        assertThat(execution.status()).isEqualTo(ExecutionStatus.RUNNING);

        execution.complete(DomainFixtures.refinementsFor(initiative, "v1"));

        assertThat(execution.status()).isEqualTo(ExecutionStatus.PENDING_APPROVAL);
        assertThat(execution.refinedStories()).hasSize(2);
        assertThat(execution.agentVersion()).isEqualTo("0.1.0");
        assertThat(execution.decision()).isNull();
    }

    @Test
    void failedExecutionKeepsTheReasonAndCannotBeDecided() {
        Execution execution = Execution.start(initiative.id(), PhaseCode.REQUIREMENTS,
                DomainFixtures.executableAgent(), "0.1.0", UUID.randomUUID(), NOW);

        execution.fail("sin respuesta del motor");

        assertThat(execution.status()).isEqualTo(ExecutionStatus.FAILED);
        assertThat(execution.failureReason()).isEqualTo("sin respuesta del motor");
        assertThat(execution.hasValidResult()).isFalse();
        assertThatThrownBy(() -> execution.decide(Decision.APPROVE, approver, null, NOW))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.EXECUTION_ALREADY_DECIDED);
    }

    @Test
    void approveDoesNotNeedAComment() {
        Execution execution = DomainFixtures.pendingExecution(initiative, "v1");

        execution.decide(Decision.APPROVE, approver, null, NOW);

        assertThat(execution.status()).isEqualTo(ExecutionStatus.ACCEPTED);
        assertThat(execution.decision().decidedBy()).isEqualTo(approver);
        assertThat(execution.decision().decidedAt()).isEqualTo(NOW);
        assertThat(execution.decision().comment()).isNull();
    }

    @Test
    void reworkAndRejectRequireAComment() {
        Execution execution = DomainFixtures.pendingExecution(initiative, "v1");

        assertThatThrownBy(() -> execution.decide(Decision.REQUEST_REWORK, approver, "   ", NOW))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.COMMENT_REQUIRED);
        assertThatThrownBy(() -> execution.decide(Decision.REJECT, approver, null, NOW))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.COMMENT_REQUIRED);
        // Un intento inválido no cambia la ejecución.
        assertThat(execution.status()).isEqualTo(ExecutionStatus.PENDING_APPROVAL);
        assertThat(execution.decision()).isNull();
    }

    @Test
    void reworkAndRejectSetTheirStatus() {
        Execution rework = DomainFixtures.pendingExecution(initiative, "v1");
        rework.decide(Decision.REQUEST_REWORK, approver, "Falta detalle", NOW);
        assertThat(rework.status()).isEqualTo(ExecutionStatus.REWORK_REQUESTED);
        assertThat(rework.hasValidResult()).isTrue();

        Execution rejected = DomainFixtures.pendingExecution(initiative, "v2");
        rejected.decide(Decision.REJECT, approver, "No aplica", NOW);
        assertThat(rejected.status()).isEqualTo(ExecutionStatus.REJECTED);
        assertThat(rejected.hasValidResult()).isFalse();
        assertThat(rejected.decision().comment()).isEqualTo("No aplica");
    }

    @Test
    void aDecidedExecutionCannotBeDecidedAgain() {
        Execution execution = DomainFixtures.pendingExecution(initiative, "v1");
        execution.decide(Decision.APPROVE, approver, null, NOW);

        assertThatThrownBy(() -> execution.decide(Decision.REJECT, approver, "cambio de opinión", NOW))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.EXECUTION_ALREADY_DECIDED);
        assertThat(execution.status()).isEqualTo(ExecutionStatus.ACCEPTED);
    }
}
