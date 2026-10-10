package de.ticketsystem.repository;

import de.ticketsystem.domain.TicketAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketAuditLogRepository extends JpaRepository<TicketAuditLog, Long> {

    List<TicketAuditLog> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}
