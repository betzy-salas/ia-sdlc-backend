package com.iasdlc.infrastructure.persistence.entity;

import com.iasdlc.domain.model.PhaseCode;
import com.iasdlc.domain.model.SquadRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "phases")
public class PhaseEntity {

    @Id
    @Enumerated(EnumType.STRING)
    PhaseCode code;

    @Column(name = "sort_order", nullable = false)
    int sortOrder;

    @Column(nullable = false)
    String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "approver_role", nullable = false)
    SquadRole approverRole;
}
