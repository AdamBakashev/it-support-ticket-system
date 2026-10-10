package de.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddCommentRequest(
        @NotNull(message = "Autor-ID ist erforderlich")
        Long authorId,

        @NotBlank(message = "Kommentartext darf nicht leer sein")
        @Size(max = 1000, message = "Kommentar darf maximal 1000 Zeichen lang sein")
        String commentText,

        boolean internal
) {
}
