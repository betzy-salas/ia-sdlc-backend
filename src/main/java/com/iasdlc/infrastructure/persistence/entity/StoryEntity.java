package com.iasdlc.infrastructure.persistence.entity;

import com.iasdlc.domain.model.StoryOrigin;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stories")
public class StoryEntity {

    @Id
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiative_id", nullable = false)
    InitiativeEntity initiative;

    @Column(nullable = false)
    String title;

    String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    StoryOrigin origin;

    /** Historia refinada vigente serializada como JSON, o null. */
    @Column(name = "refined_json")
    String refinedJson;

    @Column(name = "sort_index", nullable = false)
    int sortIndex;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;
}
