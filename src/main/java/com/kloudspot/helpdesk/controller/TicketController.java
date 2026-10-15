package com.kloudspot.helpdesk.controller;

import com.kloudspot.helpdesk.domain.*;
import com.kloudspot.helpdesk.dto.*;
import com.kloudspot.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService service;
    public TicketController(TicketService service) { this.service = service; }

    @GetMapping
    public PageResponse<TicketView> search(@AuthenticationPrincipal AppUser actor,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String assignedAgentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.search(actor, search, status, priority, assignedAgentId, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketView create(@AuthenticationPrincipal AppUser actor, @Valid @RequestBody CreateTicketRequest request) {
        return service.create(actor, request);
    }

    @GetMapping("/{id}")
    public TicketView get(@AuthenticationPrincipal AppUser actor, @PathVariable String id) { return service.get(actor, id); }

    @PutMapping("/{id}")
    public TicketView update(@AuthenticationPrincipal AppUser actor, @PathVariable String id,
                             @Valid @RequestBody UpdateTicketRequest request) { return service.update(actor, id, request); }

    @PatchMapping("/{id}/status")
    public TicketView status(@AuthenticationPrincipal AppUser actor, @PathVariable String id,
                             @Valid @RequestBody StatusRequest request) { return service.changeStatus(actor, id, request.status()); }

    @PatchMapping("/{id}/assignee")
    public TicketView assign(@AuthenticationPrincipal AppUser actor, @PathVariable String id,
                             @RequestBody AssignmentRequest request) { return service.assign(actor, id, request.agentId()); }

    @PostMapping("/{id}/comments")
    public TicketView comment(@AuthenticationPrincipal AppUser actor, @PathVariable String id,
                              @Valid @RequestBody CommentRequest request) { return service.addComment(actor, id, request); }
}
