package com.iasdlc.domain.model;

import java.util.List;

/** Historia refinada que propone el agente de la fase de requerimientos. */
public record RefinedStory(String title, String asA, String iWant, String soThat,
                           List<String> acceptanceCriteria, List<String> ambiguities) {

    public RefinedStory {
        acceptanceCriteria = acceptanceCriteria == null ? List.of() : List.copyOf(acceptanceCriteria);
        ambiguities = ambiguities == null ? List.of() : List.copyOf(ambiguities);
    }
}
