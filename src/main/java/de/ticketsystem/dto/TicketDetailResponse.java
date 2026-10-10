package de.ticketsystem.dto;

import de.ticketsystem.domain.Priority;
import de.ticketsystem.domain.Ticket;
import de.ticketsystem.domain.TicketStatus;
import java.time.LocalDateTime;
import java.util.List;

public record TicketDetailResponse(
        Long id,
        String title,
        String description,
        TicketStatus status,
        Priority priority,
        CategoryResponse category,
        UserResponse createdBy,
        UserResponse assignedTo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime resolvedAt,
        List<CommentResponse> comments,
        List<AuditLogResponse> auditLogs
) {
    public static TicketDetailResponse fromEntity(
            Ticket ticket,
            List<CommentResponse> comments,
            List<AuditLogResponse> auditLogs
    ) {
        if (ticket == null) return null;
        return new TicketDetailResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                CategoryResponse.fromEntity(ticket.getCategory()),
                UserResponse.fromEntity(ticket.getCreatedBy()),
                UserResponse.fromEntity(ticket.getAssignedTo()),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getResolvedAt(),
                comments,
                auditLogs
        );
    }
}
