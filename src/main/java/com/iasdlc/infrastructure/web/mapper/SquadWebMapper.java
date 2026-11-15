package com.iasdlc.infrastructure.web.mapper;

import com.iasdlc.application.port.in.SquadUseCase.MemberAdded;
import com.iasdlc.application.view.SquadView;
import com.iasdlc.domain.model.SquadMember;
import com.iasdlc.infrastructure.web.dto.SquadDtos.MemberResponse;
import com.iasdlc.infrastructure.web.dto.SquadDtos.SquadResponse;

public final class SquadWebMapper {

    private SquadWebMapper() {
    }

    public static SquadResponse toResponse(SquadView view) {
        return new SquadResponse(view.squad().id(), view.squad().name(),
                view.squad().members().stream()
                        .map(m -> toResponse(m, view.userNames().get(m.userId())))
                        .toList());
    }

    public static MemberResponse toResponse(MemberAdded added) {
        return toResponse(added.member(), added.fullName());
    }

    private static MemberResponse toResponse(SquadMember member, String fullName) {
        return new MemberResponse(member.id(), member.userId(), fullName, member.role(), member.substitute());
    }
}
