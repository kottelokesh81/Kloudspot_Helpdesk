package com.kloudspot.helpdesk.service;

import com.kloudspot.helpdesk.domain.*;
import com.kloudspot.helpdesk.dto.CommentRequest;
import com.kloudspot.helpdesk.dto.TicketView;
import com.kloudspot.helpdesk.error.ApiException;
import com.kloudspot.helpdesk.repository.TicketRepository;
import com.kloudspot.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {
    @Mock TicketRepository tickets;
    @Mock UserRepository users;
    @Mock MongoTemplate mongoTemplate;
    TicketService service;
    AppUser customer;
    AppUser agent;

    @BeforeEach
    void setUp() {
        service = new TicketService(tickets, users, mongoTemplate);
        customer = user("customer-1", "tenant-a", Role.CUSTOMER);
        agent = user("agent-1", "tenant-a", Role.SUPPORT_AGENT);
    }

    @Test
    void hidesInternalNotesFromCustomers() {
        Ticket ticket = ticket("ticket-1", "tenant-a", customer.getId());
        ticket.setComments(new ArrayList<>(List.of(
                new TicketComment("1", agent.getId(), agent.getName(), agent.getRole(), "Public reply", false, Instant.now()),
                new TicketComment("2", agent.getId(), agent.getName(), agent.getRole(), "Internal note", true, Instant.now()))));
        when(tickets.findByIdAndTenantId("ticket-1", "tenant-a")).thenReturn(Optional.of(ticket));

        TicketView result = service.get(customer, "ticket-1");

        assertEquals(1, result.comments().size());
        assertEquals("Public reply", result.comments().get(0).getBody());
    }

    @Test
    void customerCannotReadAnotherCustomersTicket() {
        Ticket ticket = ticket("ticket-1", "tenant-a", "different-customer");
        when(tickets.findByIdAndTenantId("ticket-1", "tenant-a")).thenReturn(Optional.of(ticket));

        ApiException error = assertThrows(ApiException.class, () -> service.get(customer, "ticket-1"));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    void rejectsInternalCommentFromCustomer() {
        Ticket ticket = ticket("ticket-1", "tenant-a", customer.getId());
        when(tickets.findByIdAndTenantId("ticket-1", "tenant-a")).thenReturn(Optional.of(ticket));

        ApiException error = assertThrows(ApiException.class,
                () -> service.addComment(customer, "ticket-1", new CommentRequest("secret", true)));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        verify(tickets, never()).save(any());
    }

    @Test
    void rejectsAssignmentToAgentFromAnotherTenant() {
        Ticket ticket = ticket("ticket-1", "tenant-a", customer.getId());
        AppUser outsider = user("agent-2", "tenant-b", Role.SUPPORT_AGENT);
        when(tickets.findByIdAndTenantId("ticket-1", "tenant-a")).thenReturn(Optional.of(ticket));
        when(users.findById("agent-2")).thenReturn(Optional.of(outsider));

        ApiException error = assertThrows(ApiException.class, () -> service.assign(agent, "ticket-1", "agent-2"));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    private AppUser user(String id, String tenant, Role role) {
        AppUser user = new AppUser(tenant, "Org", role.name(), id + "@test.dev", "hash", role);
        user.setId(id); return user;
    }

    private Ticket ticket(String id, String tenant, String requesterId) {
        Ticket ticket = new Ticket(); ticket.setId(id); ticket.setTenantId(tenant); ticket.setRequesterId(requesterId);
        ticket.setRequesterName("Requester"); ticket.setSubject("Subject"); ticket.setDescription("Description");
        ticket.setPriority(Priority.MEDIUM); ticket.setStatus(TicketStatus.OPEN);
        ticket.setComments(new ArrayList<>()); ticket.setHistory(new ArrayList<>()); return ticket;
    }
}
