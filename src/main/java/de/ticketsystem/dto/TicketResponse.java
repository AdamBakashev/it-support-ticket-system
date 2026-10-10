package de.ticketsystem.dto;

import de.ticketsystem.domain.Priority;
import de.ticketsystem.domain.Ticket;
import de.ticketsystem.domain.TicketStatus;
import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketStatus status,
        Priority priority,
        Long categoryId,
        String categoryName,
        Long createdById,
        String createdByUsername,
        Long assignedToId,
        String assignedToUsername,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime resolvedAt
) {
    public static TicketResponse fromEntity(Ticket ticket) {
        if (ticket == null) return null;
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCategory() != null ? ticket.getCategory().getId() : null,
                ticket.getCategory() != null ? ticket.getCategory().getName() : null,
                ticket.getCreatedBy() != null ? ticket.getCreatedBy().getId() : null,
                ticket.getCreatedBy() != null ? ticket.getCreatedBy().getUsername() : null,
                ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null,
                ticket.getAssignedTo() != null ? ticket.getAssignedTo().getUsername() : null,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getResolvedAt()
        );
    }
}
