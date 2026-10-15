package com.kloudspot.helpdesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
        @NotBlank
        @Pattern(regexp = "^[a-z0-9][a-z0-9-]{2,29}$", message = "must be 3-30 lowercase letters, numbers, or hyphens")
        String tenantKey,
        @NotBlank @Size(max = 120) String organizationName,
        @NotBlank @Size(max = 100) String adminName,
        @NotBlank @Email String adminEmail,
        @NotBlank @Size(min = 8, max = 100) String adminPassword) {}

