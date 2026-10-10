package de.ticketsystem.service;

import de.ticketsystem.domain.*;
import de.ticketsystem.dto.*;
import de.ticketsystem.exception.BusinessValidationException;
import de.ticketsystem.exception.ResourceNotFoundException;
import de.ticketsystem.repository.TicketAuditLogRepository;
import de.ticketsystem.repository.TicketCommentRepository;
import de.ticketsystem.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketAuditLogRepository auditLogRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TicketStateMachine stateMachine;

    public TicketService(
            TicketRepository ticketRepository,
            TicketCommentRepository commentRepository,
            TicketAuditLogRepository auditLogRepository,
            UserService userService,
            CategoryService categoryService,
            TicketStateMachine stateMachine
    ) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.auditLogRepository = auditLogRepository;
        this.userService = userService;
        this.categoryService = categoryService;
        this.stateMachine = stateMachine;
    }

    public List<TicketResponse> getAllTickets(TicketStatus status, Priority priority) {
        List<Ticket> tickets;
        if (status != null) {
            tickets = ticketRepository.findByStatus(status);
        } else if (priority != null) {
            tickets = ticketRepository.findByPriority(priority);
        } else {
            tickets = ticketRepository.findAll();
        }

        return tickets.stream()
                .map(TicketResponse::fromEntity)
                .toList();
    }

    public TicketDetailResponse getTicketDetails(Long id) {
        Ticket ticket = ticketRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket mit ID " + id + " wurde nicht gefunden."));

        List<CommentResponse> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(id).stream()
                .map(CommentResponse::fromEntity)
                .toList();

        List<AuditLogResponse> auditLogs = auditLogRepository.findByTicketIdOrderByCreatedAtDesc(id).stream()
                .map(AuditLogResponse::fromEntity)
                .toList();

        return TicketDetailResponse.fromEntity(ticket, comments, auditLogs);
    }

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        User creator = userService.findUserOrThrow(request.createdById());
        if (!creator.isActive()) {
            throw new BusinessValidationException("Inaktive Benutzer können keine Tickets erstellen.");
        }

        Category category = categoryService.findCategoryOrThrow(request.categoryId());

        Ticket ticket = new Ticket(
                request.title(),
                request.description(),
                request.priority(),
                category,
                creator
        );

        Ticket savedTicket = ticketRepository.save(ticket);

        TicketAuditLog auditLog = new TicketAuditLog(
                savedTicket,
                creator,
                null,
                TicketStatus.OPEN.name(),
                "Ticket erfolgreich erstellt"
        );
        auditLogRepository.save(auditLog);

        return TicketResponse.fromEntity(savedTicket);
    }

    @Transactional
    public TicketResponse updateTicketStatus(Long ticketId, UpdateStatusRequest request) {
        Ticket ticket = findTicketOrThrow(ticketId);
        User changer = userService.findUserOrThrow(request.changedById());

        stateMachine.validateTransition(ticket, request.newStatus());

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(request.newStatus());

        if (request.newStatus() == TicketStatus.RESOLVED || request.newStatus() == TicketStatus.CLOSED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        String summary = request.reason() != null && !request.reason().isBlank()
                ? request.reason()
                : "Status geändert von " + oldStatus + " auf " + request.newStatus();

        TicketAuditLog auditLog = new TicketAuditLog(
                ticket,
                changer,
                oldStatus.name(),
                request.newStatus().name(),
                summary
        );
        auditLogRepository.save(auditLog);

        return TicketResponse.fromEntity(ticket);
    }

    @Transactional
    public TicketResponse assignTicket(Long ticketId, AssignTicketRequest request) {
        Ticket ticket = findTicketOrThrow(ticketId);
        User assignee = userService.findUserOrThrow(request.assigneeId());
        User assignedBy = userService.findUserOrThrow(request.assignedById());

        if (assignee.getRole() == Role.EMPLOYEE) {
            throw new BusinessValidationException("Tickets können nur Support-Agents oder Administratoren zugewiesen werden.");
        }
        if (!assignee.isActive()) {
            throw new BusinessValidationException("Inaktiven Support-Mitarbeitern können keine Tickets zugewiesen werden.");
        }

        ticket.setAssignedTo(assignee);

        // Automatischer Übergang: Wird ein offenes Ticket zugewiesen, geht es in Bearbeitung
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        TicketAuditLog auditLog = new TicketAuditLog(
                ticket,
                assignedBy,
                ticket.getStatus().name(),
                ticket.getStatus().name(),
                "Ticket zugewiesen an: " + assignee.getUsername()
        );
        auditLogRepository.save(auditLog);

        return TicketResponse.fromEntity(ticket);
    }

    @Transactional
    public CommentResponse addComment(Long ticketId, AddCommentRequest request) {
        Ticket ticket = findTicketOrThrow(ticketId);
        User author = userService.findUserOrThrow(request.authorId());

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BusinessValidationException("Zu einem geschlossenen Ticket können keine neuen Kommentare hinzugefügt werden.");
        }

        // Interne Notizen dürfen nur Support-Agents oder Admins verfassen
        if (request.internal() && author.getRole() == Role.EMPLOYEE) {
            throw new BusinessValidationException("Normale Mitarbeiter dürfen keine internen Notizen erstellen.");
        }

        TicketComment comment = new TicketComment(
                ticket,
                author,
                request.commentText(),
                request.internal()
        );

        TicketComment saved = commentRepository.save(comment);
        return CommentResponse.fromEntity(saved);
    }

    public Ticket findTicketOrThrow(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket mit ID " + id + " wurde nicht gefunden."));
    }
}
