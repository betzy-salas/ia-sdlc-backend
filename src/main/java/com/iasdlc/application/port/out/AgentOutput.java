package com.iasdlc.application.port.out;

import com.iasdlc.domain.model.RefinedStory;

import java.util.List;
import java.util.UUID;

/** Salida del agente: una historia refinada por cada historia de entrada. */
public record AgentOutput(List<RefinedStoryOutput> refinedStories) {

    public record RefinedStoryOutput(UUID storyId, RefinedStory refined) {
    }
}
