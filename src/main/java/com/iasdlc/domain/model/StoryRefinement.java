package com.iasdlc.domain.model;

import java.util.UUID;

/** Resultado del agente para una historia dentro de una ejecución. */
public record StoryRefinement(UUID storyId, String originalTitle, RefinedStory refined) {
}
