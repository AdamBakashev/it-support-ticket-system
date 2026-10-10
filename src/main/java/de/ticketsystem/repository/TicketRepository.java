package de.ticketsystem.repository;

import de.ticketsystem.domain.Priority;
import de.ticketsystem.domain.Ticket;
import de.ticketsystem.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByPriority(Priority priority);

    List<Ticket> findByCreatedById(Long createdById);

    List<Ticket> findByAssignedToId(Long assignedToId);

    List<Ticket> findByCategoryId(Long categoryId);

    @Query("SELECT t FROM Ticket t " +
           "LEFT JOIN FETCH t.category " +
           "LEFT JOIN FETCH t.createdBy " +
           "LEFT JOIN FETCH t.assignedTo " +
           "WHERE t.id = :id")
    Optional<Ticket> findByIdWithDetails(@Param("id") Long id);

    long countByStatus(TicketStatus status);
}
