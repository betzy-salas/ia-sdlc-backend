package com.iasdlc.infrastructure.web.controller;

import com.iasdlc.application.port.in.PhaseQueryUseCase;
import com.iasdlc.infrastructure.web.dto.PhaseDtos.DashboardPhaseResponse;
import com.iasdlc.infrastructure.web.dto.PhaseDtos.PhaseResponse;
import com.iasdlc.infrastructure.web.mapper.PhaseWebMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PhaseController {

    private final PhaseQueryUseCase phases;

    public PhaseController(PhaseQueryUseCase phases) {
        this.phases = phases;
    }

    @GetMapping("/phases")
    public List<PhaseResponse> list() {
        return phases.listPhases().stream().map(PhaseWebMapper::toResponse).toList();
    }

    @GetMapping("/dashboard/phases")
    public List<DashboardPhaseResponse> dashboard() {
        return phases.dashboard().stream().map(PhaseWebMapper::toResponse).toList();
    }
}
