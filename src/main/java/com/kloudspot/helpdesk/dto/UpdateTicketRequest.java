package com.kloudspot.helpdesk.dto;

import com.kloudspot.helpdesk.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTicketRequest(
        @NotBlank @Size(max = 160) String subject,
        @NotBlank @Size(max = 5000) String description,
        @NotNull Priority priority) {}

