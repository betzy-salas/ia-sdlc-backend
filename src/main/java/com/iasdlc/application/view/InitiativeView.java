package com.iasdlc.application.view;

import com.iasdlc.domain.model.Initiative;
import com.iasdlc.domain.model.Project;
import com.iasdlc.domain.model.Squad;

import java.util.List;

/**
 * Iniciativa con su proyecto y el squad de ese proyecto. {@code journey} viene vacío cuando la
 * vista se usa como resumen (listados).
 */
public record InitiativeView(Initiative initiative, Project project, Squad squad, List<PhaseStepView> journey) {
}
