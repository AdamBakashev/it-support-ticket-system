package de.ticketsystem.dto;

import de.ticketsystem.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Benutzername darf nicht leer sein")
        @Size(min = 3, max = 50, message = "Benutzername muss zwischen 3 und 50 Zeichen lang sein")
        String username,

        @NotBlank(message = "E-Mail darf nicht leer sein")
        @Email(message = "Ungültiges E-Mail-Format")
        String email,

        @NotBlank(message = "Passwort darf nicht leer sein")
        @Size(min = 6, message = "Passwort muss mindestens 6 Zeichen lang sein")
        String password,

        @NotNull(message = "Rolle ist erforderlich")
        Role role,

        @NotBlank(message = "Abteilung darf nicht leer sein")
        String department
) {
}
