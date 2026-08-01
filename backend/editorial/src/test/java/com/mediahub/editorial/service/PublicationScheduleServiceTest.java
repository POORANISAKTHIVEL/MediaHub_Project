package com.mediahub.editorial.service;

import com.mediahub.editorial.model.PublicationSchedule;
import com.mediahub.editorial.repository.PublicationScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PublicationScheduleServiceTest {

    @Mock
    private PublicationScheduleRepository repository;

    @InjectMocks
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

    // TC-01: Create schedule successfully
    @Test
    void createSchedule_success() {
        when(repository.save(any(PublicationSchedule.class))).thenReturn(schedule);

        Map<String, Object> result = service.createSchedule(schedule);

        assertEquals(201, result.get("statusCode"));
        assertEquals("Scheduled", result.get("status"));
        assertEquals("Content scheduled successfully.", result.get("message"));
        verify(repository, times(1)).save(any(PublicationSchedule.class));
    }

    // TC-02: ContentID zero returns 400
    @Test
    void createSchedule_zeroContentID() {
        schedule.setContentID(0);

        Map<String, Object> result = service.createSchedule(schedule);

        assertEquals(400, result.get("statusCode"));
        assertEquals("ContentID is required", result.get("error"));
        verify(repository, never()).save(any());
    }

    // TC-03: Null publishDateTime returns 400
    @Test
    void createSchedule_nullPublishDateTime() {
        schedule.setPublishDateTime(null);

        Map<String, Object> result = service.createSchedule(schedule);

        assertEquals(400, result.get("statusCode"));
        assertEquals("PublishDateTime is required", result.get("error"));
    }

    // TC-04: Null expiryDateTime returns 400
    @Test
    void createSchedule_nullExpiryDateTime() {
        schedule.setExpiryDateTime(null);

        Map<String, Object> result = service.createSchedule(schedule);

        assertEquals(400, result.get("statusCode"));
        assertEquals("ExpiryDateTime is required", result.get("error"));
    }

    // TC-05: Null territory returns 400
    @Test
    void createSchedule_nullTerritory() {
        schedule.setTerritory(null);

        Map<String, Object> result = service.createSchedule(schedule);

        assertEquals(400, result.get("statusCode"));
        assertEquals("Territory is required", result.get("error"));
    }

    // TC-06: Empty territory returns 400
    @Test
    void createSchedule_emptyTerritory() {
        schedule.setTerritory("");

        Map<String, Object> result = service.createSchedule(schedule);

        assertEquals(400, result.get("statusCode"));
        assertEquals("Territory is required", result.get("error"));
    }

    // TC-07: Status auto-set to Scheduled before save
    @Test
    void createSchedule_statusAutoSetToScheduled() {
        schedule.setStatus(null);
        when(repository.save(any())).thenReturn(schedule);

        service.createSchedule(schedule);

        verify(repository).save(argThat(s -> "Scheduled".equals(s.getStatus())));
    }

    // TC-08: Response contains scheduleID from saved entity
    @Test
    void createSchedule_responseContainsScheduleID() {
        when(repository.save(any())).thenReturn(schedule);

        Map<String, Object> result = service.createSchedule(schedule);

        assertEquals(1, result.get("scheduleID"));
        assertEquals(101, result.get("contentID"));
        assertEquals("US", result.get("territory"));
    }

    // TC-09: Get all schedules returns populated list
    @Test
    void getAllSchedules_returnsList() {
        when(repository.findAll()).thenReturn(Arrays.asList(schedule, new PublicationSchedule()));

        List<PublicationSchedule> result = service.getAllSchedules();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    // TC-10: Get all schedules returns empty list
    @Test
    void getAllSchedules_empty() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<PublicationSchedule> result = service.getAllSchedules();

        assertTrue(result.isEmpty());
    }

    // TC-11: Get schedule by ID — found
    @Test
    void getScheduleById_found() {
        when(repository.findById(1)).thenReturn(Optional.of(schedule));

        Map<String, Object> result = service.getScheduleById(1);

        assertEquals(200, result.get("statusCode"));
        assertNotNull(result.get("schedule"));
        assertEquals(schedule, result.get("schedule"));
    }

    // TC-12: Get schedule by ID — not found
    @Test
    void getScheduleById_notFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        Map<String, Object> result = service.getScheduleById(99);

        assertEquals(404, result.get("statusCode"));
        assertTrue(result.get("error").toString().contains("99"));
    }

    // TC-13: Publish schedule — success
    @Test
    void publishSchedule_success() {
        when(repository.findById(1)).thenReturn(Optional.of(schedule));
        when(repository.save(any())).thenReturn(schedule);

        Map<String, Object> result = service.publishSchedule(1);

        assertEquals(200, result.get("statusCode"));
        assertEquals("Published", result.get("status"));
        assertEquals("Content published successfully.", result.get("message"));
        verify(repository).save(argThat(s -> "Published".equals(s.getStatus())));
    }

    // TC-14: Publish schedule — not found
    @Test
    void publishSchedule_notFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        Map<String, Object> result = service.publishSchedule(99);

        assertEquals(404, result.get("statusCode"));
        assertEquals("Schedule not found", result.get("error"));
        verify(repository, never()).save(any());
    }

    // TC-15: Cancel schedule — success
    @Test
    void cancelSchedule_success() {
        when(repository.findById(1)).thenReturn(Optional.of(schedule));
        when(repository.save(any())).thenReturn(schedule);

        Map<String, Object> result = service.cancelSchedule(1, "Budget cut");

        assertEquals(200, result.get("statusCode"));
        assertEquals("Cancelled", result.get("status"));
        assertEquals("Budget cut", result.get("reason"));
        assertEquals("Schedule cancelled successfully.", result.get("message"));
        verify(repository).save(argThat(s -> "Cancelled".equals(s.getStatus())));
    }

    // TC-16: Cancel schedule — not found
    @Test
    void cancelSchedule_notFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        Map<String, Object> result = service.cancelSchedule(99, "reason");

        assertEquals(404, result.get("statusCode"));
        assertEquals("Schedule not found", result.get("error"));
    }

    // TC-17: Delete Published schedule is blocked
    @Test
    void deleteSchedule_publishedBlocked() {
        schedule.setStatus("Published");
        when(repository.findById(1)).thenReturn(Optional.of(schedule));

        Map<String, Object> result = service.deleteSchedule(1);

        assertEquals(400, result.get("statusCode"));
        assertEquals("Cannot delete Published schedule.", result.get("error"));
        verify(repository, never()).deleteById(anyInt());
    }

    // TC-18: Delete Scheduled status succeeds
    @Test
    void deleteSchedule_scheduledSuccess() {
        schedule.setStatus("Scheduled");
        when(repository.findById(1)).thenReturn(Optional.of(schedule));
        doNothing().when(repository).deleteById(1);

        Map<String, Object> result = service.deleteSchedule(1);

        assertEquals(200, result.get("statusCode"));
        assertEquals("Schedule deleted successfully.", result.get("message"));
        verify(repository).deleteById(1);
    }

    // TC-19: Delete Cancelled status succeeds
    @Test
    void deleteSchedule_cancelledSuccess() {
        schedule.setStatus("Cancelled");
        when(repository.findById(1)).thenReturn(Optional.of(schedule));
        doNothing().when(repository).deleteById(1);

        Map<String, Object> result = service.deleteSchedule(1);

        assertEquals(200, result.get("statusCode"));
        verify(repository).deleteById(1);
    }

    // TC-20: Delete schedule — not found
    @Test
    void deleteSchedule_notFound() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        Map<String, Object> result = service.deleteSchedule(99);

        assertEquals(404, result.get("statusCode"));
        assertEquals("Schedule not found", result.get("error"));
    }
}
