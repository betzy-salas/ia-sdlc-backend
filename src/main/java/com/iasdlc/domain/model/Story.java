package com.iasdlc.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Story {

    private final UUID id;
    private final String title;
    private final String description;
    private final StoryOrigin origin;
    private final Instant createdAt;
    private RefinedStory refined;

    public Story(UUID id, String title, String description, StoryOrigin origin, RefinedStory refined,
                 Instant createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.origin = origin;
        this.refined = refined;
        this.createdAt = createdAt;
    }

    void refine(RefinedStory refinedStory) {
        this.refined = refinedStory;
    }

    public UUID id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public StoryOrigin origin() { return origin; }
    public RefinedStory refined() { return refined; }
    public Instant createdAt() { return createdAt; }
}
