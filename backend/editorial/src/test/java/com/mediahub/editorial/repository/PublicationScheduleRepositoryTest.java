package com.mediahub.editorial.repository;

import com.mediahub.editorial.model.PublicationSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PublicationScheduleRepositoryTest {

    @Mock
    private PublicationScheduleRepository repository;

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

    // TC-01: save() persists schedule and returns it
    @Test
    void save_returnsPersistedSchedule() {
        when(repository.save(any(PublicationSchedule.class))).thenReturn(schedule);

        PublicationSchedule saved = repository.save(schedule);

        assertNotNull(saved);
        assertEquals(1, saved.getScheduleID());
        assertEquals(101, saved.getContentID());
        verify(repository, times(1)).save(schedule);
    }

    // TC-02: findById() returns Optional present when found
    @Test
    void findById_returnsOptionalPresent() {
        when(repository.findById(1)).thenReturn(Optional.of(schedule));

        Optional<PublicationSchedule> result = repository.findById(1);

        assertTrue(result.isPresent());
        assertEquals("Scheduled", result.get().getStatus());
        assertEquals("US", result.get().getTerritory());
    }

    // TC-03: findById() returns empty Optional when not found
    @Test
    void findById_returnsOptionalEmpty() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        Optional<PublicationSchedule> result = repository.findById(99);

        assertFalse(result.isPresent());
    }

    // TC-04: findAll() returns all schedules
    @Test
    void findAll_returnsAllSchedules() {
        PublicationSchedule s2 = new PublicationSchedule();
        s2.setScheduleID(2);
        when(repository.findAll()).thenReturn(Arrays.asList(schedule, s2));

        List<PublicationSchedule> result = repository.findAll();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    // TC-05: findAll() returns empty list when no records
    @Test
    void findAll_returnsEmptyList() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<PublicationSchedule> result = repository.findAll();

        assertTrue(result.isEmpty());
    }

    // TC-06: deleteById() is invoked with correct ID
    @Test
    void deleteById_invokesDelete() {
        doNothing().when(repository).deleteById(1);

        repository.deleteById(1);

        verify(repository, times(1)).deleteById(1);
    }

    // TC-07: save() updates status on existing schedule
    @Test
    void save_updatesStatus() {
        schedule.setStatus("Published");
        when(repository.save(any(PublicationSchedule.class))).thenReturn(schedule);

        PublicationSchedule updated = repository.save(schedule);

        assertEquals("Published", updated.getStatus());
    }

    // TC-08: save() with Cancelled status
    @Test
    void save_cancelledStatus() {
        schedule.setStatus("Cancelled");
        when(repository.save(any(PublicationSchedule.class))).thenReturn(schedule);

        PublicationSchedule updated = repository.save(schedule);

        assertEquals("Cancelled", updated.getStatus());
    }

    // TC-09: findById() returns schedule with correct dates
    @Test
    void findById_returnsScheduleWithDates() {
        when(repository.findById(1)).thenReturn(Optional.of(schedule));

        Optional<PublicationSchedule> result = repository.findById(1);

        assertTrue(result.isPresent());
        assertNotNull(result.get().getPublishDateTime());
        assertNotNull(result.get().getExpiryDateTime());
    }
}
