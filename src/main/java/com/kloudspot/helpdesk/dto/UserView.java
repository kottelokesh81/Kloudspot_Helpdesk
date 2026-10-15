package com.kloudspot.helpdesk.dto;

import com.kloudspot.helpdesk.domain.AppUser;
import com.kloudspot.helpdesk.domain.Role;

public record UserView(String id, String tenantId, String organizationName, String name, String email, Role role) {
    public static UserView from(AppUser user) {
        return new UserView(user.getId(), user.getTenantId(), user.getOrganizationName(), user.getName(), user.getEmail(), user.getRole());
    }
}

