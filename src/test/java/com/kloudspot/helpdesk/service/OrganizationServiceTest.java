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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {
    @Mock OrganizationRepository organizations;
    @Mock UserRepository users;
    @Mock TicketRepository tickets;
    @Mock PasswordEncoder passwords;
    OrganizationService service;
    AppUser superAdmin;

    @BeforeEach
    void setUp() {
        service = new OrganizationService(organizations, users, tickets, passwords);
        superAdmin = user("platform-admin", "platform", Role.SUPER_ADMIN);
    }

    @Test
    void createsTenantAndInitialOrganizationAdmin() {
        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "northstar", "Northstar Labs", "Avery Morgan", "ADMIN@NORTHSTAR.TEST", "Temporary123!");
        when(passwords.encode("Temporary123!")).thenReturn("encoded");
        when(organizations.insert(any(Organization.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(users.insert(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser admin = invocation.getArgument(0);
            admin.setId("new-admin");
            return admin;
        });
        when(users.findFirstByTenantIdAndRole("northstar", Role.ORGANIZATION_ADMIN))
                .thenAnswer(invocation -> Optional.of(new AppUser(
                        "northstar", "Northstar Labs", "Avery Morgan", "admin@northstar.test", "encoded",
                        Role.ORGANIZATION_ADMIN)));
        when(users.countByTenantId("northstar")).thenReturn(1L);

        OrganizationView result = service.create(superAdmin, request);

        ArgumentCaptor<AppUser> admin = ArgumentCaptor.forClass(AppUser.class);
        verify(users).insert(admin.capture());
        assertEquals("northstar", admin.getValue().getTenantId());
        assertEquals(Role.ORGANIZATION_ADMIN, admin.getValue().getRole());
        assertEquals("admin@northstar.test", admin.getValue().getEmail());
        assertEquals("encoded", admin.getValue().getPasswordHash());
        assertEquals("Northstar Labs", result.name());
        assertEquals(1L, result.userCount());
    }

    @Test
    void rejectsTenantCreationByOrganizationAdmin() {
        AppUser organizationAdmin = user("org-admin", "acme", Role.ORGANIZATION_ADMIN);
        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "northstar", "Northstar Labs", "Avery Morgan", "admin@northstar.test", "Temporary123!");

        ApiException error = assertThrows(ApiException.class, () -> service.create(organizationAdmin, request));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
        verifyNoInteractions(organizations, passwords);
    }

    @Test
    void rejectsDuplicateTenantKeyWithoutChangingData() {
        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "acme", "Another Acme", "Avery Morgan", "other@acme.test", "Temporary123!");
        when(organizations.existsById("acme")).thenReturn(true);

        ApiException error = assertThrows(ApiException.class, () -> service.create(superAdmin, request));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        verify(organizations, never()).insert(any(Organization.class));
        verify(users, never()).insert(any(AppUser.class));
    }

    private AppUser user(String id, String tenant, Role role) {
        AppUser user = new AppUser(tenant, "Organization", role.name(), id + "@test.dev", "hash", role);
        user.setId(id);
        return user;
    }
}
