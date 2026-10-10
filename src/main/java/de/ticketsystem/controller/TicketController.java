package de.ticketsystem.controller;

import de.ticketsystem.domain.Priority;
import de.ticketsystem.domain.TicketStatus;
import de.ticketsystem.dto.*;
import de.ticketsystem.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets", description = "Verwaltung, Zuweisung und Statusverfolgung von Support-Tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    @Operation(summary = "Alle Tickets abrufen", description = "Liefert eine Liste aller Tickets, optional gefiltert nach Status oder Priorität.")
    public ResponseEntity<List<TicketResponse>> getAllTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) Priority priority
    ) {
        List<TicketResponse> tickets = ticketService.getAllTickets(status, priority);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ticket-Details abrufen", description = "Liefert vollständige Ticket-Details inklusive Kommentarverlauf und Status-Audit-Log.")
    public ResponseEntity<TicketDetailResponse> getTicketDetails(@PathVariable Long id) {
        TicketDetailResponse ticket = ticketService.getTicketDetails(id);
        return ResponseEntity.ok(ticket);
    }

    @PostMapping
    @Operation(summary = "Neues Support-Ticket erstellen", description = "Erstellt ein neues Ticket mit Status OPEN und initialisiert den Audit-Log.")
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketResponse created = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Ticket-Status aktualisieren", description = "Prüft Statusregeln (State Machine) und dokumentiert den Übergang im Revisions-Log.")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        TicketResponse updated = ticketService.updateTicketStatus(id, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Ticket einem Support-Agent zuweisen", description = "Weist das Ticket einem Agent zu und setzt offene Tickets automatisch auf IN_PROGRESS.")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody AssignTicketRequest request
    ) {
        TicketResponse updated = ticketService.assignTicket(id, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Kommentar hinzufügen", description = "Fügt eine öffentliche Antwort oder eine interne Notiz für das Support-Team hinzu.")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long id,
            @Valid @RequestBody AddCommentRequest request
    ) {
        CommentResponse comment = ticketService.addComment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
}
