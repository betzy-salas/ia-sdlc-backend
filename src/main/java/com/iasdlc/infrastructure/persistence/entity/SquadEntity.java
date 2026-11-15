package com.iasdlc.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "squads")
public class SquadEntity {

    @Id
    UUID id;

    @Column(nullable = false)
    String name;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @OneToMany(mappedBy = "squad", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortIndex ASC")
    List<SquadMemberEntity> members = new ArrayList<>();
}
