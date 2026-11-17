package com.iasdlc.application.usecase;

import com.iasdlc.application.port.out.SquadRepository;
import com.iasdlc.application.port.out.UserRepository;
import com.iasdlc.application.view.ExecutionView;
import com.iasdlc.application.view.MembershipView;
import com.iasdlc.application.view.SquadView;
import com.iasdlc.application.view.UserProfile;
import com.iasdlc.domain.model.Execution;
import com.iasdlc.domain.model.Initiative;
import com.iasdlc.domain.model.Project;
import com.iasdlc.domain.model.Squad;
import com.iasdlc.domain.model.SquadMember;
import com.iasdlc.domain.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Arma las vistas de lectura que comparten varios casos de uso (resuelve nombres de
 * usuarios y squads). No es un caso de uso: cada servicio crea el suyo con sus puertos.
 */
final class ViewAssembler {

    private final UserRepository users;
    private final SquadRepository squads;

    ViewAssembler(UserRepository users, SquadRepository squads) {
        this.users = users;
        this.squads = squads;
    }

    UserProfile profile(User user) {
        List<MembershipView> memberships = new ArrayList<>();
        for (Squad squad : squads.findByMemberUserId(user.id())) {
            for (SquadMember member : squad.members()) {
                if (member.userId().equals(user.id())) {
                    memberships.add(new MembershipView(squad.id(), squad.name(), member.role(),
                            member.substitute()));
                }
            }
        }
        return new UserProfile(user, memberships);
    }

    SquadView squad(Squad squad) {
        Set<UUID> ids = squad.members().stream().map(SquadMember::userId).collect(Collectors.toSet());
        return new SquadView(squad, userNames(ids));
    }

    /** {@code initiative} y {@code project} son los de la ejecución (la iniciativa y el proyecto al que pertenece). */
    ExecutionView execution(Execution execution, Initiative initiative, Project project) {
        Set<UUID> ids = new HashSet<>();
        ids.add(execution.launchedBy());
        if (execution.decision() != null) {
            ids.add(execution.decision().decidedBy());
        }
        Map<UUID, String> names = userNames(ids);
        String decidedBy = execution.decision() == null ? null : names.get(execution.decision().decidedBy());
        return new ExecutionView(execution, initiative.name(), project.id(), project.name(),
                names.get(execution.launchedBy()), decidedBy);
    }

    private Map<UUID, String> userNames(Set<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return users.findAllById(ids).stream().collect(Collectors.toMap(User::id, User::fullName));
    }
}
