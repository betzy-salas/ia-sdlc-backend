package com.iasdlc.domain;

import com.iasdlc.domain.model.Agent;
import com.iasdlc.domain.model.Execution;
import com.iasdlc.domain.model.Initiative;
import com.iasdlc.domain.model.Phase;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.domain.model.RefinedStory;
import com.iasdlc.domain.model.SquadRole;
import com.iasdlc.domain.model.Story;
import com.iasdlc.domain.model.StoryOrigin;
import com.iasdlc.domain.model.StoryRefinement;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Datos de apoyo para las pruebas de dominio. */
public final class DomainFixtures {

    public static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");
    public static final Phase REQUIREMENTS =
            new Phase(PhaseCode.REQUIREMENTS, 1, "Requerimientos", SquadRole.PRODUCT_OWNER);

    private DomainFixtures() {
    }

    public static Initiative initiativeWithStories(int count) {
        Initiative initiative = Initiative.create(UUID.randomUUID(), "Iniciativa demo", "Descripción", NOW);
        for (int i = 1; i <= count; i++) {
            initiative.addStory("Historia " + i, "Descripción " + i, StoryOrigin.EXISTING, NOW);
        }
        return initiative;
    }

    public static Agent executableAgent() {
        Agent agent = Agent.create("Agente de refinamiento", PhaseCode.REQUIREMENTS, null, null, null,
                UUID.randomUUID(), NOW);
        agent.addVersion("0.1.0", null, "instrucciones", null, NOW);
        agent.changeStatus(com.iasdlc.domain.model.AgentStatus.PILOT);
        return agent;
    }

    public static List<StoryRefinement> refinementsFor(Initiative initiative, String marker) {
        return initiative.stories().stream()
                .map(story -> new StoryRefinement(story.id(), story.title(), refined(marker + " " + story.title())))
                .toList();
    }

    public static RefinedStory refined(String title) {
        return new RefinedStory(title, "cliente", "hacer algo", "obtener valor",
                List.of("Dado A, Cuando B, Entonces C"), List.of());
    }

    /** Ejecución terminada (PENDING_APPROVAL) y ya reflejada en la iniciativa (fase AWAITING_GATE). */
    public static Execution pendingExecution(Initiative initiative, String marker) {
        Execution execution = Execution.start(initiative.id(), PhaseCode.REQUIREMENTS, executableAgent(), "0.1.0",
                UUID.randomUUID(), NOW);
        List<StoryRefinement> refinements = refinementsFor(initiative, marker);
        execution.complete(refinements);
        initiative.awaitGate(PhaseCode.REQUIREMENTS, refinements);
        return execution;
    }

    public static Story firstStory(Initiative initiative) {
        return initiative.stories().get(0);
    }
}
