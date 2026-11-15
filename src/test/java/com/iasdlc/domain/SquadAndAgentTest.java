package com.iasdlc.domain;

import com.iasdlc.domain.exception.DomainException;
import com.iasdlc.domain.exception.ErrorCode;
import com.iasdlc.domain.model.Agent;
import com.iasdlc.domain.model.AgentStatus;
import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.domain.model.Squad;
import com.iasdlc.domain.model.SquadMember;
import com.iasdlc.domain.model.SquadRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.iasdlc.domain.DomainFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SquadAndAgentTest {

    @Test
    void aUserCanHaveSeveralRolesButEachRoleOnlyOncePerSquad() {
        Squad squad = Squad.create("Squad demo", NOW);
        UUID user = UUID.randomUUID();

        squad.addMember(user, SquadRole.PRODUCT_OWNER, false);
        squad.addMember(user, SquadRole.QA, true);

        assertThat(squad.members()).hasSize(2);
        assertThatThrownBy(() -> squad.addMember(user, SquadRole.PRODUCT_OWNER, true))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.DUPLICATE_MEMBER);
    }

    @Test
    void removingAMemberTakesAwayTheRole() {
        Squad squad = Squad.create("Squad demo", NOW);
        UUID user = UUID.randomUUID();
        SquadMember member = squad.addMember(user, SquadRole.TECH_LEAD, false);

        squad.removeMember(member.id());

        assertThat(squad.hasRole(user, SquadRole.TECH_LEAD)).isFalse();
        assertThatThrownBy(() -> squad.removeMember(member.id()))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void aNewAgentIsPlannedWithoutCurrentVersion() {
        Agent agent = Agent.create("Agente", PhaseCode.REQUIREMENTS, null, null, null, UUID.randomUUID(), NOW);

        assertThat(agent.status()).isEqualTo(AgentStatus.PLANNED);
        assertThat(agent.currentVersion()).isNull();
        assertThat(agent.isExecutable()).isFalse();
    }

    @Test
    void anAgentIsExecutableOnlyInPilotOrActiveWithACurrentVersion() {
        Agent agent = Agent.create("Agente", PhaseCode.REQUIREMENTS, null, null, null, UUID.randomUUID(), NOW);

        agent.changeStatus(AgentStatus.PILOT);
        assertThat(agent.isExecutable()).isFalse();

        agent.addVersion("0.1.0", "abc123", "instrucciones", null, NOW);
        assertThat(agent.isExecutable()).isTrue();

        agent.changeStatus(AgentStatus.ACTIVE);
        assertThat(agent.isExecutable()).isTrue();

        agent.changeStatus(AgentStatus.RETIRED);
        assertThat(agent.isExecutable()).isFalse();
    }

    @Test
    void addingAVersionMakesItCurrentAndVersionsAreUnique() {
        Agent agent = Agent.create("Agente", PhaseCode.REQUIREMENTS, null, null, null, UUID.randomUUID(), NOW);

        agent.addVersion("0.1.0", null, "v1", null, NOW);
        agent.addVersion("0.2.0", null, "v2", "Mejoras", NOW);

        assertThat(agent.currentVersion()).isEqualTo("0.2.0");
        assertThat(agent.currentVersionDetail().orElseThrow().instructions()).isEqualTo("v2");
        assertThatThrownBy(() -> agent.addVersion("0.1.0", null, "otra", null, NOW))
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo(ErrorCode.DUPLICATE_NAME);
    }
}
