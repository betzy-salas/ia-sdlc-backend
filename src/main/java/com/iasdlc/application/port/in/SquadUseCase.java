package com.iasdlc.application.port.in;

import com.iasdlc.application.view.SquadView;
import com.iasdlc.domain.model.SquadMember;
import com.iasdlc.domain.model.SquadRole;

import java.util.List;
import java.util.UUID;

public interface SquadUseCase {
    List<SquadView> listSquads();
    SquadView createSquad(String name);
    SquadView getSquad(UUID squadId);
    MemberAdded addMember(UUID squadId, UUID userId, SquadRole role, boolean substitute);
    void removeMember(UUID squadId, UUID memberId);

    record MemberAdded(SquadMember member, String fullName) {
    }
}
