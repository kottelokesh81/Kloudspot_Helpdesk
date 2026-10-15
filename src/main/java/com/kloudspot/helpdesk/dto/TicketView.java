package com.kloudspot.helpdesk.dto;

import com.kloudspot.helpdesk.domain.*;
import java.time.Instant;
import java.util.List;

public record TicketView(
        String id, String subject, String description, Priority priority, TicketStatus status,
        String requesterId, String requesterName, String assignedAgentId, String assignedAgentName,
        Instant createdAt, Instant updatedAt, Instant closedAt,
        List<TicketComment> comments, List<TicketHistory> history) {

    public static TicketView from(Ticket ticket, boolean includeInternal) {
        List<TicketComment> comments = ticket.getComments() == null ? List.of() : ticket.getComments().stream()
                .filter(comment -> includeInternal || !comment.isInternal()).toList();
        return new TicketView(ticket.getId(), ticket.getSubject(), ticket.getDescription(), ticket.getPriority(),
                ticket.getStatus(), ticket.getRequesterId(), ticket.getRequesterName(), ticket.getAssignedAgentId(),
                ticket.getAssignedAgentName(), ticket.getCreatedAt(), ticket.getUpdatedAt(), ticket.getClosedAt(),
                comments, ticket.getHistory() == null ? List.of() : ticket.getHistory());
    }
}

