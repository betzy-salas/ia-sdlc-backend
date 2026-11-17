package com.iasdlc.application.port.out;

import java.util.List;
import java.util.UUID;

/**
 * Entrada del agente: la iniciativa (con el nombre de su proyecto como contexto) y sus historias
 * tal como las escribió el usuario.
 */
public record AgentInput(UUID initiativeId, String initiativeName, String initiativeDescription,
                         String projectName, List<StoryInput> stories) {

    public record StoryInput(UUID storyId, String title, String description) {
    }
}
