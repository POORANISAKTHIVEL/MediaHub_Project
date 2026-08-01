package com.mediahub.notification.controller;

import com.mediahub.notification.dto.response.NotificationResponseDTO;
import com.mediahub.notification.entity.Notification;
import com.mediahub.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService service;

    private static final String BASE = "/mediaHub/notifications";

    private NotificationResponseDTO sampleResponse() {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setNotificationId(1L);
        dto.setUserId(1L);
        dto.setMessage("Hello");
        dto.setCategory(Notification.Category.CONTENT);
        dto.setStatus(Notification.Status.UNREAD);
        dto.setCreatedDate(LocalDateTime.now());
        return dto;
    }

    // ---------- POST /createNotification/v1.0 ----------

    @Test
    void createNotification_returns201WithCreatedNotification() throws Exception {
        String requestJson = """
                {
                    "userId": 1,
                    "message": "Hello",
                    "category": "CONTENT"
                }
                """;

        when(service.createNotification(any()))
                .thenReturn(sampleResponse());

        mockMvc.perform(post(BASE + "/createNotification/v1.0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Notification created successfully"));
    }

    // ---------- GET /getAllNotifications/v1.0/{userId} ----------

    @Test
    void getAllNotifications_returns200WithList() throws Exception {
        when(service.getAllNotifications(1L))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get(BASE + "/getAllNotifications/v1.0/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].notificationId").value(1));
    }

    @Test
    void getAllNotifications_returns200WithEmptyListWhenNone() throws Exception {
        when(service.getAllNotifications(42L))
                .thenReturn(List.of());

        mockMvc.perform(get(BASE + "/getAllNotifications/v1.0/{userId}", 42L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---------- GET /getUnreadNotifications/v1.0/{userId} ----------

    @Test
    void getUnreadNotifications_returns200WithUnreadList() throws Exception {
        when(service.getUnreadNotifications(1L))
                .thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get(BASE + "/getUnreadNotifications/v1.0/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("UNREAD"));
    }

    // ---------- PUT /updateNotification/v1.0/{id}?status= ----------

    @Test
    void updateNotification_returns200WhenStatusUpdated() throws Exception {
        NotificationResponseDTO updated = sampleResponse();
        updated.setStatus(Notification.Status.READ);

        when(service.updateNotification(eq(1L), eq("read")))
                .thenReturn(updated);

        mockMvc.perform(put(BASE + "/updateNotification/v1.0/{id}", 1L)
                        .param("status", "read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification updated successfully"));
    }

    @Test
    void updateNotification_returns404WhenNotificationNotFound() throws Exception {

        when(service.updateNotification(eq(5L), eq("read")))
                .thenThrow(new NoSuchElementException(
                        "Notification not found with id 5"));

        mockMvc.perform(put(BASE + "/updateNotification/v1.0/{id}", 5L)
                        .param("status", "read"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("Not Found")));
}

    @Test
    void updateNotification_returns400WhenNotificationDismissed() throws Exception {
        when(service.updateNotification(eq(9L), eq("read")))
                .thenThrow(new RuntimeException(
                        "Dismissed notification cannot be updated"));

        mockMvc.perform(put(BASE + "/updateNotification/v1.0/{id}", 9L)
                        .param("status", "read"))
                .andExpect(status().isBadRequest());
    }
}
