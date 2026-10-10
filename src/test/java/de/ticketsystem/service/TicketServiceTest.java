package de.ticketsystem.service;

import de.ticketsystem.domain.*;
import de.ticketsystem.dto.AssignTicketRequest;
import de.ticketsystem.dto.CreateTicketRequest;
import de.ticketsystem.dto.TicketResponse;
import de.ticketsystem.dto.UpdateStatusRequest;
import de.ticketsystem.exception.BusinessValidationException;
import de.ticketsystem.exception.InvalidStatusTransitionException;
import de.ticketsystem.repository.TicketAuditLogRepository;
import de.ticketsystem.repository.TicketCommentRepository;
import de.ticketsystem.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketCommentRepository commentRepository;

    @Mock
    private TicketAuditLogRepository auditLogRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @Spy
    private TicketStateMachine stateMachine = new TicketStateMachine();

    @InjectMocks
    private TicketService ticketService;

    private User employee;
    private User supportAgent;
    private Category category;
    private Ticket openTicket;

    @BeforeEach
    void setUp() {
        employee = new User("dev.schmidt", "schmidt@company.de", "hash", Role.EMPLOYEE, "Dev");
        employee.setId(1L);

        supportAgent = new User("agent.meyer", "meyer@company.de", "hash", Role.SUPPORT_AGENT, "Support");
        supportAgent.setId(2L);

        category = new Category("Hardware", "Hardware-Probleme");
        category.setId(1L);

        openTicket = new Ticket("Monitor defekt", "Bildschirm flackert", Priority.HIGH, category, employee);
        openTicket.setId(10L);
    }

    @Test
    @DisplayName("Ticket erfolgreich erstellen und initialen Audit-Log schreiben")
    void createTicket_success() {
        CreateTicketRequest request = new CreateTicketRequest(
                "Monitor defekt", "Bildschirm flackert", Priority.HIGH, 1L, 1L
        );

        when(userService.findUserOrThrow(1L)).thenReturn(employee);
        when(categoryService.findCategoryOrThrow(1L)).thenReturn(category);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setId(100L);
            return t;
        });

        TicketResponse response = ticketService.createTicket(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo(TicketStatus.OPEN);
        assertThat(response.title()).isEqualTo("Monitor defekt");

        verify(auditLogRepository, times(1)).save(any(TicketAuditLog.class));
    }

    @Test
    @DisplayName("Gültiger Statuswechsel: OPEN -> IN_PROGRESS")
    void updateStatus_validTransition_success() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(openTicket));
        when(userService.findUserOrThrow(2L)).thenReturn(supportAgent);

        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.IN_PROGRESS, 2L, "Ticket übernommen");

        TicketResponse response = ticketService.updateTicketStatus(10L, request);

        assertThat(response.status()).isEqualTo(TicketStatus.IN_PROGRESS);
        verify(auditLogRepository, times(1)).save(any(TicketAuditLog.class));
    }

    @Test
    @DisplayName("Ungültiger Statuswechsel: CLOSED -> IN_PROGRESS wirft InvalidStatusTransitionException")
    void updateStatus_invalidTransition_throwsException() {
        openTicket.setStatus(TicketStatus.CLOSED);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(openTicket));
        when(userService.findUserOrThrow(2L)).thenReturn(supportAgent);

        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.IN_PROGRESS, 2L, "Wiedereröffnung");

        assertThatThrownBy(() -> ticketService.updateTicketStatus(10L, request))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("Ungültiger Statusübergang von 'CLOSED' zu 'IN_PROGRESS'");

        verify(auditLogRepository, never()).save(any(TicketAuditLog.class));
    }

    @Test
    @DisplayName("Ticket auf RESOLVED setzen ohne Bearbeiter wirft BusinessValidationException")
    void updateStatus_resolveWithoutAssignee_throwsBusinessValidationException() {
        openTicket.setStatus(TicketStatus.IN_PROGRESS);
        openTicket.setAssignedTo(null);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(openTicket));
        when(userService.findUserOrThrow(2L)).thenReturn(supportAgent);

        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.RESOLVED, 2L, "Fertig");

        assertThatThrownBy(() -> ticketService.updateTicketStatus(10L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Ein Ticket kann nicht gelöst ('RESOLVED') werden, ohne dass ein Support-Agent zugewiesen ist");
    }

    @Test
    @DisplayName("Zuweisung an Support-Agent schaltet OPEN Ticket automatisch auf IN_PROGRESS")
    void assignTicket_success_transitionsOpenToInProgress() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(openTicket));
        when(userService.findUserOrThrow(2L)).thenReturn(supportAgent);
        when(userService.findUserOrThrow(1L)).thenReturn(employee);

        AssignTicketRequest request = new AssignTicketRequest(2L, 1L);

        TicketResponse response = ticketService.assignTicket(10L, request);

        assertThat(response.assignedToUsername()).isEqualTo("agent.meyer");
        assertThat(response.status()).isEqualTo(TicketStatus.IN_PROGRESS);
        verify(auditLogRepository, times(1)).save(any(TicketAuditLog.class));
    }

    @Test
    @DisplayName("Zuweisung an normalen Mitarbeiter (EMPLOYEE) wird verweigert")
    void assignTicket_toRegularEmployee_throwsBusinessValidationException() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(openTicket));
        when(userService.findUserOrThrow(1L)).thenReturn(employee);
        when(userService.findUserOrThrow(2L)).thenReturn(supportAgent);

        AssignTicketRequest request = new AssignTicketRequest(1L, 2L);

        assertThatThrownBy(() -> ticketService.assignTicket(10L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Tickets können nur Support-Agents oder Administratoren zugewiesen werden.");
    }
}
