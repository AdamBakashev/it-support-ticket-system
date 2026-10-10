package de.ticketsystem.dto;

import jakarta.validation.constraints.NotNull;

public record AssignTicketRequest(
        @NotNull(message = "Zugewiesene Benutzer-ID (Support Agent) ist erforderlich")
        Long assigneeId,

        @NotNull(message = "ID des zuweisenden Benutzers ist erforderlich")
        Long assignedById
) {
}
