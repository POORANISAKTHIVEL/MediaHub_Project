package com.mediahub.editorial.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediahub.editorial.model.PublicationSchedule;
import com.mediahub.editorial.service.PublicationScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicationScheduleController.class)
public class PublicationScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PublicationScheduleService service;

    private PublicationSchedule schedule;

    @BeforeEach
    void setUp() {
        schedule = new PublicationSchedule();
        schedule.setScheduleID(1);
        schedule.setContentID(101);
        schedule.setPublishDateTime(new Date());
        schedule.setExpiryDateTime(new Date(System.currentTimeMillis() + 86400000L));
        schedule.setTerritory("US");
        schedule.setStatus("Scheduled");
    }

    // TC-01: POST /schedules — 201 Created
    @Test
    void createSchedule_returns201() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 201);
        resp.put("scheduleID", 1);
        resp.put("status", "Scheduled");
        resp.put("message", "Content scheduled successfully.");
        when(service.createSchedule(any(PublicationSchedule.class))).thenReturn(resp);

        mockMvc.perform(post("/MediaHub/editorial/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(schedule)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("Scheduled"))
                .andExpect(jsonPath("$.message").value("Content scheduled successfully."));
    }

    // TC-02: POST /schedules — 400 Bad Request (missing field)
    @Test
    void createSchedule_returns400() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 400);
        resp.put("error", "Territory is required");
        when(service.createSchedule(any(PublicationSchedule.class))).thenReturn(resp);

        mockMvc.perform(post("/MediaHub/editorial/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(schedule)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Territory is required"));
    }

    // TC-03: GET /schedules — 200 with list
    @Test
    void getAllSchedules_returns200() throws Exception {
        when(service.getAllSchedules())
                .thenReturn(Arrays.asList(schedule, new PublicationSchedule()));

        mockMvc.perform(get("/MediaHub/editorial/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // TC-04: GET /schedules — 200 empty list
    @Test
    void getAllSchedules_returnsEmptyList() throws Exception {
        when(service.getAllSchedules()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/MediaHub/editorial/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // TC-05: GET /schedules/{scheduleID} — 200 Found
    @Test
    void getScheduleById_returns200() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 200);
        resp.put("schedule", schedule);
        when(service.getScheduleById(1)).thenReturn(resp);

        mockMvc.perform(get("/MediaHub/editorial/schedules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schedule").exists());
    }

    // TC-06: GET /schedules/{scheduleID} — 404 Not Found
    @Test
    void getScheduleById_returns404() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 404);
        resp.put("error", "Schedule not found with ID: 99");
        when(service.getScheduleById(99)).thenReturn(resp);

        mockMvc.perform(get("/MediaHub/editorial/schedules/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Schedule not found with ID: 99"));
    }

    // TC-07: POST /schedules/{scheduleID}/publish — 200 Published
    @Test
    void publishSchedule_returns200() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 200);
        resp.put("scheduleID", 1);
        resp.put("status", "Published");
        resp.put("message", "Content published successfully.");
        when(service.publishSchedule(1)).thenReturn(resp);

        mockMvc.perform(post("/MediaHub/editorial/schedules/1/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Published"))
                .andExpect(jsonPath("$.message").value("Content published successfully."));
    }

    // TC-08: POST /schedules/{scheduleID}/publish — 404 Not Found
    @Test
    void publishSchedule_returns404() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 404);
        resp.put("error", "Schedule not found");
        when(service.publishSchedule(99)).thenReturn(resp);

        mockMvc.perform(post("/MediaHub/editorial/schedules/99/publish"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Schedule not found"));
    }

    // TC-09: POST /schedules/{scheduleID}/cancel — 200 Cancelled
    @Test
    void cancelSchedule_returns200() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 200);
        resp.put("status", "Cancelled");
        resp.put("reason", "Budget cut");
        resp.put("message", "Schedule cancelled successfully.");
        when(service.cancelSchedule(eq(1), anyString())).thenReturn(resp);

        Map<String, String> body = Map.of("reason", "Budget cut");
        mockMvc.perform(post("/MediaHub/editorial/schedules/1/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Cancelled"))
                .andExpect(jsonPath("$.reason").value("Budget cut"))
                .andExpect(jsonPath("$.message").value("Schedule cancelled successfully."));
    }

    // TC-10: POST /schedules/{scheduleID}/cancel — 404 Not Found
    @Test
    void cancelSchedule_returns404() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 404);
        resp.put("error", "Schedule not found");
        when(service.cancelSchedule(eq(99), anyString())).thenReturn(resp);

        Map<String, String> body = Map.of("reason", "reason");
        mockMvc.perform(post("/MediaHub/editorial/schedules/99/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Schedule not found"));
    }

    // TC-11: DELETE /schedules/{scheduleID} — 200 Deleted
    @Test
    void deleteSchedule_returns200() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 200);
        resp.put("message", "Schedule deleted successfully.");
        when(service.deleteSchedule(1)).thenReturn(resp);

        mockMvc.perform(delete("/MediaHub/editorial/schedules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Schedule deleted successfully."));
    }

    // TC-12: DELETE /schedules/{scheduleID} — 400 Published blocked
    @Test
    void deleteSchedule_publishedBlocked_returns400() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 400);
        resp.put("error", "Cannot delete Published schedule.");
        when(service.deleteSchedule(1)).thenReturn(resp);

        mockMvc.perform(delete("/MediaHub/editorial/schedules/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot delete Published schedule."));
    }

    // TC-13: DELETE /schedules/{scheduleID} — 404 Not Found
    @Test
    void deleteSchedule_returns404() throws Exception {
        Map<String, Object> resp = new HashMap<>();
        resp.put("statusCode", 404);
        resp.put("error", "Schedule not found");
        when(service.deleteSchedule(99)).thenReturn(resp);

        mockMvc.perform(delete("/MediaHub/editorial/schedules/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Schedule not found"));
    }
}
