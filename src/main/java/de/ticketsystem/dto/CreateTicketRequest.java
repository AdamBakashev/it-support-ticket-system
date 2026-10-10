package de.ticketsystem.dto;

import de.ticketsystem.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank(message = "Titel darf nicht leer sein")
        @Size(max = 150, message = "Titel darf maximal 150 Zeichen lang sein")
        String title,

        @NotBlank(message = "Beschreibung darf nicht leer sein")
        String description,

        @NotNull(message = "Priorität ist erforderlich")
        Priority priority,

        @NotNull(message = "Kategorie-ID ist erforderlich")
        Long categoryId,

        @NotNull(message = "Ersteller-ID ist erforderlich")
        Long createdById
) {
}
