package com.iasdlc.domain.model;

import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Project {

    private final UUID id;
    private final String name;
    private final String description;
    private final UUID squadId;
    private PhaseCode currentPhase;
    private final Instant createdAt;
    private final List<Story> stories;
    private final Map<PhaseCode, PhaseProgress> progress;

    public Project(UUID id, String name, String description, UUID squadId, PhaseCode currentPhase,
                   Instant createdAt, List<Story> stories, Map<PhaseCode, PhaseProgress> progress) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.squadId = squadId;
        this.currentPhase = currentPhase;
        this.createdAt = createdAt;
        this.stories = new ArrayList<>(stories);
        this.progress = new EnumMap<>(PhaseCode.class);
        for (PhaseCode code : PhaseCode.values()) {
            this.progress.put(code, progress.getOrDefault(code, PhaseProgress.PENDING));
        }
    }

    /** Un proyecto nuevo arranca en REQUIREMENTS (IN_PROGRESS); las demás fases quedan PENDING. */
    public static Project create(String name, String description, UUID squadId, Instant now) {
        return new Project(UUID.randomUUID(), name.trim(), description, squadId, PhaseCode.REQUIREMENTS, now,
                List.of(), Map.of(PhaseCode.REQUIREMENTS, PhaseProgress.IN_PROGRESS));
    }

    // --- Historias -------------------------------------------------------------------------

    public Story addStory(String title, String description, StoryOrigin origin, Instant now) {
        ensureStoriesEditable();
        Story story = new Story(UUID.randomUUID(), title.trim(), description, origin, null, now);
        stories.add(story);
        return story;
    }

    public void removeStory(UUID storyId) {
        ensureStoriesEditable();
        boolean removed = stories.removeIf(s -> s.id().equals(storyId));
        if (!removed) {
            throw DomainException.notFound("la historia en el proyecto");
        }
    }

    /** Solo se agregan o borran historias mientras REQUIREMENTS esté IN_PROGRESS. */
    private void ensureStoriesEditable() {
        PhaseProgress requirements = progressOf(PhaseCode.REQUIREMENTS);
        if (requirements == PhaseProgress.AWAITING_GATE) {
            throw new DomainException(ErrorCode.PHASE_AWAITING_GATE,
                    "No se pueden modificar las historias: hay una ejecución pendiente de aprobación");
        }
        if (requirements != PhaseProgress.IN_PROGRESS) {
            throw new DomainException(ErrorCode.PHASE_ALREADY_APPROVED,
                    "No se pueden modificar las historias: la fase de requerimientos ya fue aprobada");
        }
    }

    // --- Ejecución y gate ------------------------------------------------------------------

    /** Valida que la fase se pueda ejecutar ahora. */
    public void ensureExecutable(PhaseCode phase) {
        PhaseProgress current = progressOf(phase);
        if (current == PhaseProgress.APPROVED) {
            throw new DomainException(ErrorCode.PHASE_ALREADY_APPROVED, "La fase ya fue aprobada");
        }
        if (current == PhaseProgress.AWAITING_GATE) {
            throw new DomainException(ErrorCode.PHASE_AWAITING_GATE,
                    "Ya hay una ejecución de esta fase pendiente de aprobación");
        }
        if (current == PhaseProgress.PENDING) {
            throw new DomainException(ErrorCode.VALIDATION_ERROR, "El proyecto todavía no llega a esta fase");
        }
        if (stories.isEmpty()) {
            throw new DomainException(ErrorCode.NO_STORIES, "El proyecto no tiene historias para ejecutar la fase");
        }
    }

    /** La fase queda a la espera del gate y las historias reflejan el resultado propuesto. */
    public void awaitGate(PhaseCode phase, List<StoryRefinement> refinements) {
        progress.put(phase, PhaseProgress.AWAITING_GATE);
        applyRefinements(refinements);
    }

    /** Gate aprobado: la fase queda APPROVED y el proyecto avanza a la siguiente (IN_PROGRESS). */
    public void approvePhase(PhaseCode phase) {
        progress.put(phase, PhaseProgress.APPROVED);
        phase.next().ifPresent(next -> {
            currentPhase = next;
            progress.put(next, PhaseProgress.IN_PROGRESS);
        });
    }

    /** Gate con reproceso o rechazo: la fase vuelve a IN_PROGRESS. */
    public void reopenPhase(PhaseCode phase) {
        progress.put(phase, PhaseProgress.IN_PROGRESS);
    }

    /**
     * Deja en cada historia el refinamiento indicado; las historias que no aparecen quedan sin refinar.
     * Se usa para que {@code story.refined} refleje la última ejecución no rechazada.
     */
    public void applyRefinements(List<StoryRefinement> refinements) {
        for (Story story : stories) {
            RefinedStory refined = refinements.stream()
                    .filter(r -> r.storyId().equals(story.id()))
                    .map(StoryRefinement::refined)
                    .findFirst()
                    .orElse(null);
            story.refine(refined);
        }
    }

    public PhaseProgress progressOf(PhaseCode phase) {
        return progress.get(phase);
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public String description() { return description; }
    public UUID squadId() { return squadId; }
    public PhaseCode currentPhase() { return currentPhase; }
    public Instant createdAt() { return createdAt; }
    public List<Story> stories() { return List.copyOf(stories); }
    public Map<PhaseCode, PhaseProgress> progress() { return Map.copyOf(progress); }
}
