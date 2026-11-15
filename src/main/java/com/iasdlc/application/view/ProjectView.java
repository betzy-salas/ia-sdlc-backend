package com.iasdlc.application.view;

import com.iasdlc.domain.model.Project;
import com.iasdlc.domain.model.Squad;

import java.util.List;

public record ProjectView(Project project, Squad squad, List<PhaseStepView> journey) {
}
