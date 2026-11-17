package com.iasdlc.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class ProjectEntity {

    @Id
    UUID id;

    @Column(nullable = false)
    String name;

    String description;

    @Column(name = "squad_id", nullable = false)
    UUID squadId;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;
}
