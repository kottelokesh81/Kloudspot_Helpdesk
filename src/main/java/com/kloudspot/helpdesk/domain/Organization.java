package com.kloudspot.helpdesk.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("organizations")
public class Organization {
    @Id private String id;
    @Indexed(unique = true) private String name;
    private Instant createdAt;
    private String createdBy;

    public Organization() {}

    public Organization(String id, String name, Instant createdAt, String createdBy) {
        this.id = id; this.name = name; this.createdAt = createdAt; this.createdBy = createdBy;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}

