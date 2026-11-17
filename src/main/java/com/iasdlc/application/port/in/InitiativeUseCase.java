package com.iasdlc.application.port.in;

import com.iasdlc.application.view.InitiativeView;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.domain.model.Story;
import com.iasdlc.domain.model.StoryOrigin;

import java.util.List;
import java.util.UUID;

public interface InitiativeUseCase {
    InitiativeView createInitiative(UUID projectId, String name, String description);
    /** Lista iniciativas (sin recorrido); los filtros nulos no se aplican. {@code phase} compara contra la fase actual. */
    List<InitiativeView> listInitiatives(UUID projectId, PhaseCode phase);
    InitiativeView getInitiative(UUID initiativeId);
    Story addStory(UUID initiativeId, String title, String description, StoryOrigin origin);
    void removeStory(UUID initiativeId, UUID storyId);
}
