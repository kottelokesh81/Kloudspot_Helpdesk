package com.kloudspot.helpdesk.dto;

import java.time.Instant;

public record OrganizationView(
        String id,
        String name,
        Instant createdAt,
        String adminName,
        String adminEmail,
        long userCount,
        long ticketCount) {}

