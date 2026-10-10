package de.ticketsystem.dto;

import de.ticketsystem.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateStatusRequest(
        @NotNull(message = "Neuer Status ist erforderlich")
        TicketStatus newStatus,

        @NotNull(message = "ID des ändernden Benutzers ist erforderlich")
        Long changedById,

        @Size(max = 255, message = "Begründung darf maximal 255 Zeichen lang sein")
        String reason
) {
}
