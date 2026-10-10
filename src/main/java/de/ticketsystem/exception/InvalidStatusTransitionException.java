package de.ticketsystem.exception;

import de.ticketsystem.domain.TicketStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(TicketStatus from, TicketStatus to) {
        super("Ungültiger Statusübergang von '" + from + "' zu '" + to + "'");
    }

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
