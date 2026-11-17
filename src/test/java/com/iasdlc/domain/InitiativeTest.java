package com.iasdlc.domain;

import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;
import com.iasdlc.domain.model.Initiative;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.domain.model.PhaseProgress;
import com.iasdlc.domain.model.StoryOrigin;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.iasdlc.domain.DomainFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InitiativeTest {

    @Test
    void newInitiativeStartsInRequirementsInProgress() {
        Initiative initiative = Initiative.create(UUID.randomUUID(), "Iniciativa", null, NOW);

        assertThat(initiative.currentPhase()).isEqualTo(PhaseCode.REQUIREMENTS);
        assertThat(initiative.progressOf(PhaseCode.REQUIREMENTS)).isEqualTo(PhaseProgress.IN_PROGRESS);
        assertThat(initiative.progressOf(PhaseCode.ANALYSIS)).isEqualTo(PhaseProgress.PENDING);
        assertThat(initiative.progressOf(PhaseCode.TESTING)).isEqualTo(PhaseProgress.PENDING);
        assertThat(initiative.stories()).isEmpty();
    }

    @Test
    void newInitiativeBelongsToItsProjectAndIsNotCompleted() {
        UUID projectId = UUID.randomUUID();

        Initiative initiative = Initiative.create(projectId, "  Lote 1  ", "Primer lote", NOW);

        assertThat(initiative.projectId()).isEqualTo(projectId);
        assertThat(initiative.name()).isEqualTo("Lote 1");
        assertThat(initiative.completed()).isFalse();
    }

    @Test
    void initiativesOfTheSameProjectAdvanceIndependently() {
        UUID projectId = UUID.randomUUID();
        Initiative first = Initiative.create(projectId, "Lote 1", null, NOW);
        Initiative second = Initiative.create(projectId, "Lote 2", null, NOW);
        first.addStory("Historia", null, StoryOrigin.EXISTING, NOW);

        first.approvePhase(PhaseCode.REQUIREMENTS);

        assertThat(first.currentPhase()).isEqualTo(PhaseCode.ANALYSIS);
        assertThat(second.currentPhase()).isEqualTo(PhaseCode.REQUIREMENTS);
        assertThat(second.progressOf(PhaseCode.REQUIREMENTS)).isEqualTo(PhaseProgress.IN_PROGRESS);
        assertThat(second.stories()).isEmpty();
    }

    @Test
    void initiativeIsCompletedWhenTheLastPhaseIsApproved() {
        Initiative initiative = DomainFixtures.initiativeWithStories(1);

        for (PhaseCode phase : PhaseCode.values()) {
            assertThat(initiative.completed()).isFalse();
            initiative.approvePhase(phase);
        }

        assertThat(initiative.completed()).isTrue();
        assertThat(initiative.currentPhase()).isEqualTo(PhaseCode.TESTING);
    }

    @Test
    void cannotExecuteWithoutStories() {
        Initiative initiative = DomainFixtures.initiativeWithStories(0);

        assertThatThrownBy(() -> initiative.ensureExecutable(PhaseCode.REQUIREMENTS))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.NO_STORIES);
    }

    @Test
    void cannotExecuteWhileAwaitingGate() {
        Initiative initiative = DomainFixtures.initiativeWithStories(1);
        initiative.awaitGate(PhaseCode.REQUIREMENTS, DomainFixtures.refinementsFor(initiative, "v1"));

        assertThatThrownBy(() -> initiative.ensureExecutable(PhaseCode.REQUIREMENTS))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.PHASE_AWAITING_GATE);
    }

    @Test
    void cannotExecuteAnApprovedPhase() {
        Initiative initiative = DomainFixtures.initiativeWithStories(1);
        initiative.approvePhase(PhaseCode.REQUIREMENTS);

        assertThatThrownBy(() -> initiative.ensureExecutable(PhaseCode.REQUIREMENTS))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.PHASE_ALREADY_APPROVED);
    }

    @Test
    void storiesCanOnlyChangeWhileRequirementsIsInProgress() {
        Initiative initiative = DomainFixtures.initiativeWithStories(1);
        UUID storyId = DomainFixtures.firstStory(initiative).id();
        initiative.awaitGate(PhaseCode.REQUIREMENTS, DomainFixtures.refinementsFor(initiative, "v1"));

        assertThatThrownBy(() -> initiative.addStory("Otra", null, StoryOrigin.ASSISTED, NOW))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.PHASE_AWAITING_GATE);
        assertThatThrownBy(() -> initiative.removeStory(storyId))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.PHASE_AWAITING_GATE);

        initiative.approvePhase(PhaseCode.REQUIREMENTS);

        assertThatThrownBy(() -> initiative.addStory("Otra", null, StoryOrigin.ASSISTED, NOW))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.PHASE_ALREADY_APPROVED);
    }

    @Test
    void removingAnUnknownStoryIsNotFound() {
        Initiative initiative = DomainFixtures.initiativeWithStories(1);

        assertThatThrownBy(() -> initiative.removeStory(UUID.randomUUID()))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void approvingMovesToTheNextPhase() {
        Initiative initiative = DomainFixtures.initiativeWithStories(1);

        initiative.approvePhase(PhaseCode.REQUIREMENTS);

        assertThat(initiative.progressOf(PhaseCode.REQUIREMENTS)).isEqualTo(PhaseProgress.APPROVED);
        assertThat(initiative.currentPhase()).isEqualTo(PhaseCode.ANALYSIS);
        assertThat(initiative.progressOf(PhaseCode.ANALYSIS)).isEqualTo(PhaseProgress.IN_PROGRESS);
        assertThat(initiative.progressOf(PhaseCode.DESIGN)).isEqualTo(PhaseProgress.PENDING);
    }

    @Test
    void awaitingGateShowsTheProposalOnEachStory() {
        Initiative initiative = DomainFixtures.initiativeWithStories(2);

        initiative.awaitGate(PhaseCode.REQUIREMENTS, DomainFixtures.refinementsFor(initiative, "v1"));

        assertThat(initiative.progressOf(PhaseCode.REQUIREMENTS)).isEqualTo(PhaseProgress.AWAITING_GATE);
        assertThat(initiative.stories()).allSatisfy(story ->
                assertThat(story.refined().title()).isEqualTo("v1 " + story.title()));
    }

    @Test
    void applyingNoRefinementsClearsTheStories() {
        Initiative initiative = DomainFixtures.initiativeWithStories(2);
        initiative.applyRefinements(DomainFixtures.refinementsFor(initiative, "v1"));

        initiative.applyRefinements(List.of());

        assertThat(initiative.stories()).allSatisfy(story -> assertThat(story.refined()).isNull());
    }
}
