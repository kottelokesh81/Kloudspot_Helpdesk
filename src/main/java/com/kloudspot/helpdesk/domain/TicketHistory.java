package com.kloudspot.helpdesk.domain;

import java.time.Instant;

public class TicketHistory {
    private String id;
    private String actorId;
    private String actorName;
    private String field;
    private String fromValue;
    private String toValue;
    private Instant createdAt;

    public TicketHistory() {}
    public TicketHistory(String id, String actorId, String actorName, String field, String fromValue, String toValue, Instant createdAt) {
        this.id = id; this.actorId = actorId; this.actorName = actorName; this.field = field;
        this.fromValue = fromValue; this.toValue = toValue; this.createdAt = createdAt;
    }
    public String getId() { return id; }
    public String getActorId() { return actorId; }
    public String getActorName() { return actorName; }
    public String getField() { return field; }
    public String getFromValue() { return fromValue; }
    public String getToValue() { return toValue; }
    public Instant getCreatedAt() { return createdAt; }
}

