package com.kloudspot.helpdesk.controller;

import com.kloudspot.helpdesk.domain.AppUser;
import com.kloudspot.helpdesk.dto.CreateOrganizationRequest;
import com.kloudspot.helpdesk.dto.OrganizationView;
import com.kloudspot.helpdesk.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {
    private final OrganizationService service;
    public OrganizationController(OrganizationService service) { this.service = service; }

    @GetMapping
    public List<OrganizationView> list(@AuthenticationPrincipal AppUser actor) { return service.list(actor); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationView create(@AuthenticationPrincipal AppUser actor,
                                   @Valid @RequestBody CreateOrganizationRequest request) {
        return service.create(actor, request);
    }
}
