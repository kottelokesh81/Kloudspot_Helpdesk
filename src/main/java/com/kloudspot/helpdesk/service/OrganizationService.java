package com.kloudspot.helpdesk.service;

import com.kloudspot.helpdesk.domain.AppUser;
import com.kloudspot.helpdesk.domain.Organization;
import com.kloudspot.helpdesk.domain.Role;
import com.kloudspot.helpdesk.dto.CreateOrganizationRequest;
import com.kloudspot.helpdesk.dto.OrganizationView;
import com.kloudspot.helpdesk.error.ApiException;
import com.kloudspot.helpdesk.repository.OrganizationRepository;
import com.kloudspot.helpdesk.repository.TicketRepository;
import com.kloudspot.helpdesk.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class OrganizationService {
    private final OrganizationRepository organizations;
    private final UserRepository users;
    private final TicketRepository tickets;
    private final PasswordEncoder passwords;

    public OrganizationService(OrganizationRepository organizations, UserRepository users,
                               TicketRepository tickets, PasswordEncoder passwords) {
        this.organizations = organizations; this.users = users; this.tickets = tickets; this.passwords = passwords;
    }

    public List<OrganizationView> list(AppUser actor) {
        requireSuperAdmin(actor);
        return organizations.findAllByOrderByCreatedAtDesc().stream().map(this::view).toList();
    }

    public OrganizationView create(AppUser actor, CreateOrganizationRequest request) {
        requireSuperAdmin(actor);
        String tenantKey = request.tenantKey().trim().toLowerCase();
        String organizationName = request.organizationName().trim();
        String adminEmail = request.adminEmail().trim().toLowerCase();
        if (organizations.existsById(tenantKey))
            throw new ApiException(HttpStatus.CONFLICT, "Tenant key is already in use");
        if (organizations.existsByNameIgnoreCase(organizationName))
            throw new ApiException(HttpStatus.CONFLICT, "Organization name is already in use");
        if (users.findByEmailIgnoreCase(adminEmail).isPresent())
            throw new ApiException(HttpStatus.CONFLICT, "Administrator email is already in use");

        Organization organization = new Organization(tenantKey, organizationName, Instant.now(), actor.getId());
        boolean organizationCreated = false;
        try {
            organizations.insert(organization);
            organizationCreated = true;
            AppUser admin = new AppUser(tenantKey, organizationName, request.adminName().trim(), adminEmail,
                    passwords.encode(request.adminPassword()), Role.ORGANIZATION_ADMIN);
            users.insert(admin);
        } catch (DuplicateKeyException ex) {
            if (organizationCreated) organizations.deleteById(tenantKey);
            throw new ApiException(HttpStatus.CONFLICT, "Tenant key, organization name, or email is already in use");
        } catch (RuntimeException ex) {
            if (organizationCreated) organizations.deleteById(tenantKey);
            throw ex;
        }
        return view(organization);
    }

    private OrganizationView view(Organization organization) {
        AppUser admin = users.findFirstByTenantIdAndRole(organization.getId(), Role.ORGANIZATION_ADMIN).orElse(null);
        return new OrganizationView(organization.getId(), organization.getName(), organization.getCreatedAt(),
                admin == null ? null : admin.getName(), admin == null ? null : admin.getEmail(),
                users.countByTenantId(organization.getId()), tickets.countByTenantId(organization.getId()));
    }

    private void requireSuperAdmin(AppUser actor) {
        if (actor.getRole() != Role.SUPER_ADMIN)
            throw new ApiException(HttpStatus.FORBIDDEN, "This action is restricted to the platform Super Admin");
    }
}

