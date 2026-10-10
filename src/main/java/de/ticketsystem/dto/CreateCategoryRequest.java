package de.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank(message = "Kategoriename darf nicht leer sein")
        @Size(max = 50, message = "Kategoriename darf maximal 50 Zeichen lang sein")
        String name,

        @Size(max = 255, message = "Beschreibung darf maximal 255 Zeichen lang sein")
        String description
) {
}
