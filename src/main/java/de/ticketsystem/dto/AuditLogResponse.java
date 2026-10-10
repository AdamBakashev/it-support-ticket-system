package de.ticketsystem.dto;

import de.ticketsystem.domain.TicketAuditLog;
import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long changedById,
        String changedByUsername,
        String oldStatus,
        String newStatus,
        String changeSummary,
        LocalDateTime createdAt
) {
    public static AuditLogResponse fromEntity(TicketAuditLog log) {
        if (log == null) return null;
        return new AuditLogResponse(
                log.getId(),
                log.getChangedBy() != null ? log.getChangedBy().getId() : null,
                log.getChangedBy() != null ? log.getChangedBy().getUsername() : null,
                log.getOldStatus(),
                log.getNewStatus(),
                log.getChangeSummary(),
                log.getCreatedAt()
        );
    }
}
