package com.kloudspot.helpdesk.dto;

import com.kloudspot.helpdesk.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull TicketStatus status) {}

