package com.iasdlc.domain;

import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;
import com.iasdlc.domain.model.Decision;
import com.iasdlc.domain.model.Execution;
import com.iasdlc.domain.model.ExecutionStatus;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.domain.model.PhaseProgress;
import com.iasdlc.domain.model.Project;
import com.iasdlc.domain.model.Squad;
import com.iasdlc.domain.model.SquadRole;
import com.iasdlc.domain.service.GateDecisionService;
import com.iasdlc.domain.service.GatePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static com.iasdlc.domain.DomainFixtures.NOW;
import static com.iasdlc.domain.DomainFixtures.REQUIREMENTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GateDecisionServiceTest {

    private final UUID productOwner = UUID.randomUUID();
    private final UUID substituteOwner = UUID.randomUUID();
    private final UUID developer = UUID.randomUUID();
    private final UUID outsider = UUID.randomUUID();
    private Squad squad;
    private Project project;

    @BeforeEach
    void setUp() {
        squad = Squad.create("Squad demo", NOW);
        squad.addMember(productOwner, SquadRole.PRODUCT_OWNER, false);
        squad.addMember(substituteOwner, SquadRole.PRODUCT_OWNER, true);
        squad.addMember(developer, SquadRole.DEVELOPER, false);
        project = DomainFixtures.projectWithStories(2);
    }

    @Test
    void approverIsASquadMemberWithThePhaseRoleHolderOrSubstitute() {
        assertThat(GatePolicy.isApprover(squad, REQUIREMENTS, productOwner)).isTrue();
        assertThat(GatePolicy.isApprover(squad, REQUIREMENTS, substituteOwner)).isTrue();
        assertThat(GatePolicy.isApprover(squad, REQUIREMENTS, developer)).isFalse();
        assertThat(GatePolicy.isApprover(squad, REQUIREMENTS, outsider)).isFalse();
    }

    @Test
    void aNonApproverCannotDecideAndNothingChanges() {
        Execution execution = DomainFixtures.pendingExecution(project, "v1");

        assertThatThrownBy(() -> GateDecisionService.decide(project, squad, REQUIREMENTS, execution, developer,
                Decision.APPROVE, null, NOW, Optional.empty()))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.NOT_GATE_APPROVER);

        assertThat(execution.status()).isEqualTo(ExecutionStatus.PENDING_APPROVAL);
        assertThat(project.progressOf(PhaseCode.REQUIREMENTS)).isEqualTo(PhaseProgress.AWAITING_GATE);
    }

    @Test
    void approveAcceptsTheExecutionAndAdvancesTheProject() {
        Execution execution = DomainFixtures.pendingExecution(project, "v1");

        GateDecisionService.decide(project, squad, REQUIREMENTS, execution, substituteOwner, Decision.APPROVE,
                null, NOW, Optional.empty());

        assertThat(execution.status()).isEqualTo(ExecutionStatus.ACCEPTED);
        assertThat(project.progressOf(PhaseCode.REQUIREMENTS)).isEqualTo(PhaseProgress.APPROVED);
        assertThat(project.currentPhase()).isEqualTo(PhaseCode.ANALYSIS);
        assertThat(project.progressOf(PhaseCode.ANALYSIS)).isEqualTo(PhaseProgress.IN_PROGRESS);
        assertThat(DomainFixtures.firstStory(project).refined().title()).startsWith("v1");
    }

    @Test
    void reworkReopensThePhaseAndKeepsTheProposalOnTheStories() {
        Execution execution = DomainFixtures.pendingExecution(project, "v1");

        GateDecisionService.decide(project, squad, REQUIREMENTS, execution, productOwner,
                Decision.REQUEST_REWORK, "Aclarar el actor", NOW, Optional.empty());

        assertThat(execution.status()).isEqualTo(ExecutionStatus.REWORK_REQUESTED);
        assertThat(project.progressOf(PhaseCode.REQUIREMENTS)).isEqualTo(PhaseProgress.IN_PROGRESS);
        assertThat(project.currentPhase()).isEqualTo(PhaseCode.REQUIREMENTS);
        assertThat(DomainFixtures.firstStory(project).refined().title()).startsWith("v1");
    }

    @Test
    void rejectWithoutPreviousExecutionClearsTheStories() {
        Execution execution = DomainFixtures.pendingExecution(project, "v1");

        GateDecisionService.decide(project, squad, REQUIREMENTS, execution, productOwner, Decision.REJECT,
                "No corresponde", NOW, Optional.empty());

        assertThat(execution.status()).isEqualTo(ExecutionStatus.REJECTED);
        assertThat(project.progressOf(PhaseCode.REQUIREMENTS)).isEqualTo(PhaseProgress.IN_PROGRESS);
        assertThat(project.stories()).allSatisfy(story -> assertThat(story.refined()).isNull());
    }

    @Test
    void rejectRestoresTheLastNonRejectedExecution() {
        Execution first = DomainFixtures.pendingExecution(project, "v1");
        GateDecisionService.decide(project, squad, REQUIREMENTS, first, productOwner, Decision.REQUEST_REWORK,
                "Mejorar criterios", NOW, Optional.empty());
        Execution second = DomainFixtures.pendingExecution(project, "v2");
        assertThat(DomainFixtures.firstStory(project).refined().title()).startsWith("v2");

        GateDecisionService.decide(project, squad, REQUIREMENTS, second, productOwner, Decision.REJECT,
                "Peor que la anterior", NOW, Optional.of(first));

        assertThat(DomainFixtures.firstStory(project).refined().title()).startsWith("v1");
    }
}
