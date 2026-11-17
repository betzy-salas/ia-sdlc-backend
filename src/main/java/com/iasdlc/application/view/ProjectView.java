package com.iasdlc.application.view;

import com.iasdlc.domain.model.Project;
import com.iasdlc.domain.model.Squad;

import java.util.List;

/** Proyecto con su squad y sus iniciativas (en forma de resumen, sin recorrido). */
public record ProjectView(Project project, Squad squad, List<InitiativeView> initiatives) {
}
