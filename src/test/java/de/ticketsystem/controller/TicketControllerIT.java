package de.ticketsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ticketsystem.domain.Priority;
import de.ticketsystem.domain.TicketStatus;
import de.ticketsystem.dto.CreateTicketRequest;
import de.ticketsystem.dto.UpdateStatusRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TicketControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/tickets liefert Testdaten aus data.sql")
    void getAllTickets_returnsSeededTickets() throws Exception {
        mockMvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$[0].title", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/v1/tickets/1 liefert Ticket-Details inklusive Kommentare")
    void getTicketDetails_returnsDetails() throws Exception {
        mockMvc.perform(get("/api/v1/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", containsString("VPN-Verbindung")))
                .andExpect(jsonPath("$.comments", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.auditLogs", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("POST /api/v1/tickets mit gültigen Daten erstellt neues Ticket (201 Created)")
    void createTicket_valid_returnsCreated() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest(
                "Neuer Firmenlaptop fährt nicht hoch",
                "Nach dem Auspacken des Lenovo ThinkPads bleibt das Display schwarz.",
                Priority.HIGH,
                1L, // Hardware
                4L  // dev.schmidt
        );

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.title", is("Neuer Firmenlaptop fährt nicht hoch")));
    }

    @Test
    @DisplayName("POST /api/v1/tickets mit fehlendem Titel liefert 400 Bad Request mit Fehlermeldung")
    void createTicket_blankTitle_returnsBadRequest() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest(
                "", // Ungültig: Blank
                "Beschreibung ohne Titel",
                Priority.LOW,
                1L,
                4L
        );

        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.title", notNullValue()));
    }

    @Test
    @DisplayName("PATCH /api/v1/tickets/4/status auf bereits geschlossenes Ticket liefert 409 Conflict")
    void updateStatus_onClosedTicket_returnsConflict() throws Exception {
        // Ticket 4 ist in data.sql bereits CLOSED
        UpdateStatusRequest request = new UpdateStatusRequest(
                TicketStatus.IN_PROGRESS,
                1L,
                "Wiedereröffnung versuchen"
        );

        mockMvc.perform(patch("/api/v1/tickets/4/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("Ungültiger Statusübergang von 'CLOSED'")));
    }
}
