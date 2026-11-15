package com.iasdlc.application.usecase;

import com.iasdlc.application.UseCase;
import com.iasdlc.application.port.in.SquadUseCase;
import com.iasdlc.application.port.out.SquadRepository;
import com.iasdlc.application.port.out.UserRepository;
import com.iasdlc.application.view.SquadView;
import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;
import com.iasdlc.domain.model.Squad;
import com.iasdlc.domain.model.SquadMember;
import com.iasdlc.domain.model.SquadRole;
import com.iasdlc.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@UseCase
@Transactional
public class SquadService implements SquadUseCase {

    private final SquadRepository squads;
    private final UserRepository users;
    private final Clock clock;
    private final ViewAssembler views;

    public SquadService(SquadRepository squads, UserRepository users, Clock clock) {
        this.squads = squads;
        this.users = users;
        this.clock = clock;
        this.views = new ViewAssembler(users, squads);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SquadView> listSquads() {
        return squads.findAll().stream().map(views::squad).toList();
    }

    @Override
    public SquadView createSquad(String name) {
        if (squads.existsByName(name.trim())) {
            throw new DomainException(ErrorCode.DUPLICATE_NAME, "Ya existe un squad con ese nombre");
        }
        return views.squad(squads.save(Squad.create(name, clock.instant())));
    }

    @Override
    @Transactional(readOnly = true)
    public SquadView getSquad(UUID squadId) {
        return views.squad(load(squadId));
    }

    @Override
    public MemberAdded addMember(UUID squadId, UUID userId, SquadRole role, boolean substitute) {
        Squad squad = load(squadId);
        User user = users.findById(userId).orElseThrow(() -> DomainException.notFound("el usuario"));
        SquadMember member = squad.addMember(user.id(), role, substitute);
        squads.save(squad);
        return new MemberAdded(member, user.fullName());
    }

    @Override
    public void removeMember(UUID squadId, UUID memberId) {
        Squad squad = load(squadId);
        squad.removeMember(memberId);
        squads.save(squad);
    }

    private Squad load(UUID squadId) {
        return squads.findById(squadId).orElseThrow(() -> DomainException.notFound("el squad"));
    }
}
