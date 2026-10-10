package de.ticketsystem.dto;

import de.ticketsystem.domain.Role;
import de.ticketsystem.domain.User;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        String department,
        boolean active,
        LocalDateTime createdAt
) {
    public static UserResponse fromEntity(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getDepartment(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
