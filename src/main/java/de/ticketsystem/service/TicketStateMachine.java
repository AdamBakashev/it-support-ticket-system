package de.ticketsystem.service;

import de.ticketsystem.domain.Ticket;
import de.ticketsystem.domain.TicketStatus;
import de.ticketsystem.exception.BusinessValidationException;
import de.ticketsystem.exception.InvalidStatusTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Validiert Statusübergänge im Ticket-Lebenszyklus.
 * Verhindert inkonsistente Zustände und erzwingt Domänenregeln.
 */
@Component
public class TicketStateMachine {

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
            TicketStatus.OPEN, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED),
            TicketStatus.IN_PROGRESS, EnumSet.of(TicketStatus.WAITING_FOR_USER, TicketStatus.RESOLVED, TicketStatus.CLOSED),
            TicketStatus.WAITING_FOR_USER, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, TicketStatus.CLOSED),
            TicketStatus.RESOLVED, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED),
            TicketStatus.CLOSED, EnumSet.noneOf(TicketStatus.class)
    );

    public void validateTransition(Ticket ticket, TicketStatus targetStatus) {
        TicketStatus currentStatus = ticket.getStatus();

        if (currentStatus == targetStatus) {
            return; // Idempotent: Keine Änderung
        }

        Set<TicketStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(targetStatus)) {
            throw new InvalidStatusTransitionException(currentStatus, targetStatus);
        }

        // Domänenregel: Ein Ticket kann nur gelöst werden, wenn ein Bearbeiter zugewiesen ist
        if (targetStatus == TicketStatus.RESOLVED && ticket.getAssignedTo() == null) {
            throw new BusinessValidationException(
                    "Ein Ticket kann nicht gelöst ('RESOLVED') werden, ohne dass ein Support-Agent zugewiesen ist."
            );
        }
    }
}
