package com.iasdlc.infrastructure.web.controller;

import com.iasdlc.application.port.in.SquadUseCase;
import com.iasdlc.infrastructure.web.dto.SquadDtos.AddMemberRequest;
import com.iasdlc.infrastructure.web.dto.SquadDtos.CreateSquadRequest;
import com.iasdlc.infrastructure.web.dto.SquadDtos.MemberResponse;
import com.iasdlc.infrastructure.web.dto.SquadDtos.SquadResponse;
import com.iasdlc.infrastructure.web.mapper.SquadWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/squads")
public class SquadController {

    private final SquadUseCase squads;

    public SquadController(SquadUseCase squads) {
        this.squads = squads;
    }

    @GetMapping
    public List<SquadResponse> list() {
        return squads.listSquads().stream().map(SquadWebMapper::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SquadResponse create(@Valid @RequestBody CreateSquadRequest request) {
        return SquadWebMapper.toResponse(squads.createSquad(request.name()));
    }

    @GetMapping("/{squadId}")
    public SquadResponse get(@PathVariable("squadId") UUID squadId) {
        return SquadWebMapper.toResponse(squads.getSquad(squadId));
    }

    @PostMapping("/{squadId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse addMember(@PathVariable("squadId") UUID squadId,
                                    @Valid @RequestBody AddMemberRequest request) {
        boolean substitute = Boolean.TRUE.equals(request.substitute());
        return SquadWebMapper.toResponse(
                squads.addMember(squadId, request.userId(), request.role(), substitute));
    }

    @DeleteMapping("/{squadId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable("squadId") UUID squadId, @PathVariable("memberId") UUID memberId) {
        squads.removeMember(squadId, memberId);
    }
}
