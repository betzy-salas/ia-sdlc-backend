package com.iasdlc.domain;

import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.domain.model.PhaseProgress;
import com.iasdlc.domain.model.Project;
import com.iasdlc.domain.model.StoryOrigin;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.iasdlc.domain.DomainFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectTest {

    @Test
    void newProjectStartsInRequirementsInProgress() {
        Project project = Project.create("Proyecto", null, UUID.randomUUID(), NOW);

        assertThat(project.currentPhase()).isEqualTo(PhaseCode.REQUIREMENTS);
        assertThat(project.progressOf(PhaseCode.REQUIREMENTS)).isEqualTo(PhaseProgress.IN_PROGRESS);
        assertThat(project.progressOf(PhaseCode.ANALYSIS)).isEqualTo(PhaseProgress.PENDING);
        assertThat(project.progressOf(PhaseCode.TESTING)).isEqualTo(PhaseProgress.PENDING);
        assertThat(project.stories()).isEmpty();
    }

    @Test
    void cannotExecuteWithoutStories() {
        Project project = DomainFixtures.projectWithStories(0);

        assertThatThrownBy(() -> project.ensureExecutable(PhaseCode.REQUIREMENTS))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.NO_STORIES);
    }

    @Test
    void cannotExecuteWhileAwaitingGate() {
        Project project = DomainFixtures.projectWithStories(1);
        project.awaitGate(PhaseCode.REQUIREMENTS, DomainFixtures.refinementsFor(project, "v1"));

        assertThatThrownBy(() -> project.ensureExecutable(PhaseCode.REQUIREMENTS))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.PHASE_AWAITING_GATE);
    }

    @Test
    void cannotExecuteAnApprovedPhase() {
        Project project = DomainFixtures.projectWithStories(1);
        project.approvePhase(PhaseCode.REQUIREMENTS);

        assertThatThrownBy(() -> project.ensureExecutable(PhaseCode.REQUIREMENTS))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.PHASE_ALREADY_APPROVED);
    }

    @Test
    void storiesCanOnlyChangeWhileRequirementsIsInProgress() {
        Project project = DomainFixtures.projectWithStories(1);
        UUID storyId = DomainFixtures.firstStory(project).id();
        project.awaitGate(PhaseCode.REQUIREMENTS, DomainFixtures.refinementsFor(project, "v1"));

        assertThatThrownBy(() -> project.addStory("Otra", null, StoryOrigin.ASSISTED, NOW))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.PHASE_AWAITING_GATE);
        assertThatThrownBy(() -> project.removeStory(storyId))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.PHASE_AWAITING_GATE);

        project.approvePhase(PhaseCode.REQUIREMENTS);

        assertThatThrownBy(() -> project.addStory("Otra", null, StoryOrigin.ASSISTED, NOW))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.PHASE_ALREADY_APPROVED);
    }

    @Test
    void removingAnUnknownStoryIsNotFound() {
        Project project = DomainFixtures.projectWithStories(1);

        assertThatThrownBy(() -> project.removeStory(UUID.randomUUID()))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void approvingMovesToTheNextPhase() {
        Project project = DomainFixtures.projectWithStories(1);

        project.approvePhase(PhaseCode.REQUIREMENTS);

        assertThat(project.progressOf(PhaseCode.REQUIREMENTS)).isEqualTo(PhaseProgress.APPROVED);
        assertThat(project.currentPhase()).isEqualTo(PhaseCode.ANALYSIS);
        assertThat(project.progressOf(PhaseCode.ANALYSIS)).isEqualTo(PhaseProgress.IN_PROGRESS);
        assertThat(project.progressOf(PhaseCode.DESIGN)).isEqualTo(PhaseProgress.PENDING);
    }

    @Test
    void awaitingGateShowsTheProposalOnEachStory() {
        Project project = DomainFixtures.projectWithStories(2);

        project.awaitGate(PhaseCode.REQUIREMENTS, DomainFixtures.refinementsFor(project, "v1"));

        assertThat(project.progressOf(PhaseCode.REQUIREMENTS)).isEqualTo(PhaseProgress.AWAITING_GATE);
        assertThat(project.stories()).allSatisfy(story ->
                assertThat(story.refined().title()).isEqualTo("v1 " + story.title()));
    }

    @Test
    void applyingNoRefinementsClearsTheStories() {
        Project project = DomainFixtures.projectWithStories(2);
        project.applyRefinements(DomainFixtures.refinementsFor(project, "v1"));

        project.applyRefinements(List.of());

        assertThat(project.stories()).allSatisfy(story -> assertThat(story.refined()).isNull());
    }
}
