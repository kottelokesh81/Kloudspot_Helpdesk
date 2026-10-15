package com.kloudspot.helpdesk.service;

import com.kloudspot.helpdesk.domain.*;
import com.kloudspot.helpdesk.dto.*;
import com.kloudspot.helpdesk.error.ApiException;
import com.kloudspot.helpdesk.repository.TicketRepository;
import com.kloudspot.helpdesk.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class TicketService {
    private final TicketRepository tickets;
    private final UserRepository users;
    private final MongoTemplate mongoTemplate;

    public TicketService(TicketRepository tickets, UserRepository users, MongoTemplate mongoTemplate) {
        this.tickets = tickets; this.users = users; this.mongoTemplate = mongoTemplate;
    }

    public PageResponse<TicketView> search(AppUser actor, String text, TicketStatus status, Priority priority,
                                            String assignedAgentId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        List<Criteria> filters = new ArrayList<>();
        filters.add(Criteria.where("tenantId").is(actor.getTenantId()));
        if (actor.getRole() == Role.CUSTOMER) filters.add(Criteria.where("requesterId").is(actor.getId()));
        if (text != null && !text.isBlank()) {
            String escaped = java.util.regex.Pattern.quote(text.trim());
            filters.add(new Criteria().orOperator(
                    Criteria.where("subject").regex(escaped, "i"),
                    Criteria.where("description").regex(escaped, "i")));
        }
        if (status != null) filters.add(Criteria.where("status").is(status));
        if (priority != null) filters.add(Criteria.where("priority").is(priority));
        if (assignedAgentId != null && !assignedAgentId.isBlank())
            filters.add(Criteria.where("assignedAgentId").is(assignedAgentId));

        Query base = new Query(new Criteria().andOperator(filters.toArray(Criteria[]::new)));
        long total = mongoTemplate.count(base, Ticket.class);
        Query paged = Query.of(base).with(PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
        List<TicketView> content = mongoTemplate.find(paged, Ticket.class).stream()
                .map(ticket -> TicketView.from(ticket, actor.getRole() != Role.CUSTOMER)).toList();
        return new PageResponse<>(content, safePage, safeSize, total, (int) Math.ceil((double) total / safeSize));
    }

    public TicketView create(AppUser actor, CreateTicketRequest request) {
        if (actor.getRole() == Role.SUPER_ADMIN)
            throw new ApiException(HttpStatus.FORBIDDEN, "Platform administrators do not belong to a tenant workspace");
        Ticket ticket = new Ticket();
        ticket.setTenantId(actor.getTenantId()); ticket.setSubject(request.subject().trim());
        ticket.setDescription(request.description().trim()); ticket.setPriority(request.priority());
        ticket.setStatus(TicketStatus.OPEN); ticket.setRequesterId(actor.getId()); ticket.setRequesterName(actor.getName());
        Instant now = Instant.now(); ticket.setCreatedAt(now); ticket.setUpdatedAt(now);
        ticket.getHistory().add(history(actor, "status", null, TicketStatus.OPEN.name()));
        return TicketView.from(tickets.save(ticket), actor.getRole() != Role.CUSTOMER);
    }

    public TicketView get(AppUser actor, String id) {
        return TicketView.from(requireAccessible(actor, id), actor.getRole() != Role.CUSTOMER);
    }

    public TicketView update(AppUser actor, String id, UpdateTicketRequest request) {
        requireStaff(actor);
        Ticket ticket = requireAccessible(actor, id);
        ticket.setSubject(request.subject().trim()); ticket.setDescription(request.description().trim());
        ticket.setPriority(request.priority()); ticket.setUpdatedAt(Instant.now());
        return TicketView.from(tickets.save(ticket), true);
    }

    public TicketView changeStatus(AppUser actor, String id, TicketStatus status) {
        requireStaff(actor);
        Ticket ticket = requireAccessible(actor, id);
        TicketStatus previous = ticket.getStatus();
        if (previous != status) {
            ticket.setStatus(status); ticket.setUpdatedAt(Instant.now());
            ticket.setClosedAt(status == TicketStatus.CLOSED ? Instant.now() : null);
            ticket.getHistory().add(history(actor, "status", previous.name(), status.name()));
            ticket = tickets.save(ticket);
        }
        return TicketView.from(ticket, true);
    }

    public TicketView assign(AppUser actor, String id, String agentId) {
        requireStaff(actor);
        Ticket ticket = requireAccessible(actor, id);
        String oldName = ticket.getAssignedAgentName();
        if (agentId == null || agentId.isBlank()) {
            ticket.setAssignedAgentId(null); ticket.setAssignedAgentName(null);
        } else {
            AppUser agent = users.findById(agentId)
                    .filter(user -> user.getTenantId().equals(actor.getTenantId()) && user.getRole() == Role.SUPPORT_AGENT)
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Agent does not belong to this organization"));
            ticket.setAssignedAgentId(agent.getId()); ticket.setAssignedAgentName(agent.getName());
        }
        if (!Objects.equals(oldName, ticket.getAssignedAgentName())) {
            ticket.getHistory().add(history(actor, "assignment", oldName, ticket.getAssignedAgentName()));
            ticket.setUpdatedAt(Instant.now()); ticket = tickets.save(ticket);
        }
        return TicketView.from(ticket, true);
    }

    public TicketView addComment(AppUser actor, String id, CommentRequest request) {
        Ticket ticket = requireAccessible(actor, id);
        if (actor.getRole() == Role.CUSTOMER && request.internal())
            throw new ApiException(HttpStatus.FORBIDDEN, "Customers cannot add internal notes");
        ticket.getComments().add(new TicketComment(UUID.randomUUID().toString(), actor.getId(), actor.getName(),
                actor.getRole(), request.body().trim(), request.internal(), Instant.now()));
        ticket.setUpdatedAt(Instant.now());
        return TicketView.from(tickets.save(ticket), actor.getRole() != Role.CUSTOMER);
    }

    private Ticket requireAccessible(AppUser actor, String id) {
        Ticket ticket = tickets.findByIdAndTenantId(id, actor.getTenantId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ticket not found"));
        if (actor.getRole() == Role.CUSTOMER && !actor.getId().equals(ticket.getRequesterId()))
            throw new ApiException(HttpStatus.NOT_FOUND, "Ticket not found");
        return ticket;
    }

    private void requireStaff(AppUser actor) {
        if (actor.getRole() != Role.ORGANIZATION_ADMIN && actor.getRole() != Role.SUPPORT_AGENT)
            throw new ApiException(HttpStatus.FORBIDDEN, "This action is restricted to helpdesk staff");
    }

    private TicketHistory history(AppUser actor, String field, String from, String to) {
        return new TicketHistory(UUID.randomUUID().toString(), actor.getId(), actor.getName(), field, from, to, Instant.now());
    }
}

