package com.kloudspot.helpdesk.domain;

import java.time.Instant;

public class TicketComment {
    private String id;
    private String authorId;
    private String authorName;
    private Role authorRole;
    private String body;
    private boolean internal;
    private Instant createdAt;

    public TicketComment() {}
    public TicketComment(String id, String authorId, String authorName, Role authorRole, String body, boolean internal, Instant createdAt) {
        this.id = id; this.authorId = authorId; this.authorName = authorName; this.authorRole = authorRole;
        this.body = body; this.internal = internal; this.createdAt = createdAt;
    }
    public String getId() { return id; }
    public String getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public Role getAuthorRole() { return authorRole; }
    public String getBody() { return body; }
    public boolean isInternal() { return internal; }
    public Instant getCreatedAt() { return createdAt; }
}

