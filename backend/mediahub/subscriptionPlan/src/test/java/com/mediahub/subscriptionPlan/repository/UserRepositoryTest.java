package com.mediahub.subscriptionPlan.repository;

import com.mediahub.subscriptionPlan.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .userId(1L)
                .name("Alice Fernandez")
                .roles("Admin")
                .email("alice@mediahub.io")
                .phone("+91-9000000001")
                .country("India")
                .status("Active")
                .build();

        user2 = User.builder()
                .userId(2L)
                .name("Bob Mathew")
                .roles("User")
                .email("bob@mediahub.io")
                .phone("+91-9000000002")
                .country("India")
                .status("Active")
                .build();
    }

    // ── Save Tests ────────────────────────────────────────────────

    @Test
    void save_User_Success() {
        when(userRepository.save(any(User.class)))
                .thenReturn(user1);

        User saved = userRepository.save(user1);

        assertNotNull(saved);
        assertEquals(1L, saved.getUserId());
        assertEquals("Alice Fernandez", saved.getName());
        assertEquals("alice@mediahub.io", saved.getEmail());
        verify(userRepository, times(1)).save(user1);
    }

    // ── FindAll Tests ─────────────────────────────────────────────

    @Test
    void findAll_ReturnsAllUsers() {
        when(userRepository.findAll())
                .thenReturn(Arrays.asList(user1, user2));

        List<User> users = userRepository.findAll();

        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("Alice Fernandez", users.get(0).getName());
        assertEquals("Bob Mathew", users.get(1).getName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void findAll_ReturnsEmptyList() {
        when(userRepository.findAll())
                .thenReturn(Arrays.asList());

        List<User> users = userRepository.findAll();

        assertNotNull(users);
        assertEquals(0, users.size());
    }

    // ── FindById Tests ────────────────────────────────────────────

    @Test
    void findById_ReturnsUser_WhenExists() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));

        Optional<User> result = userRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Alice Fernandez", result.get().getName());
        assertEquals("alice@mediahub.io", result.get().getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void findById_ReturnsEmpty_WhenNotExists() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<User> result = userRepository.findById(99L);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(99L);
    }

    // ── FindByEmail Tests ─────────────────────────────────────────

    @Test
    void findByEmail_ReturnsUser_WhenExists() {
        when(userRepository.findByEmail("alice@mediahub.io"))
                .thenReturn(Optional.of(user1));

        Optional<User> result = userRepository.findByEmail("alice@mediahub.io");

        assertTrue(result.isPresent());
        assertEquals("Alice Fernandez", result.get().getName());
        assertEquals(1L, result.get().getUserId());
        verify(userRepository, times(1)).findByEmail("alice@mediahub.io");
    }

    @Test
    void findByEmail_ReturnsEmpty_WhenNotExists() {
        when(userRepository.findByEmail("unknown@mediahub.io"))
                .thenReturn(Optional.empty());

        Optional<User> result = userRepository.findByEmail("unknown@mediahub.io");

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByEmail("unknown@mediahub.io");
    }

    // ── Count Tests ───────────────────────────────────────────────

    @Test
    void count_ReturnsCorrectCount() {
        when(userRepository.count())
                .thenReturn(2L);

        long count = userRepository.count();

        assertEquals(2L, count);
        verify(userRepository, times(1)).count();
    }

    @Test
    void count_ReturnsZero_WhenEmpty() {
        when(userRepository.count())
                .thenReturn(0L);

        long count = userRepository.count();

        assertEquals(0L, count);
    }

    // ── Delete Tests ──────────────────────────────────────────────

    @Test
    void deleteById_DeletesUser_Successfully() {
        doNothing().when(userRepository).deleteById(1L);

        userRepository.deleteById(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    // ── ExistsById Tests ──────────────────────────────────────────

    @Test
    void existsById_ReturnsTrue_WhenExists() {
        when(userRepository.existsById(1L))
                .thenReturn(true);

        boolean exists = userRepository.existsById(1L);

        assertTrue(exists);
        verify(userRepository, times(1)).existsById(1L);
    }

    @Test
    void existsById_ReturnsFalse_WhenNotExists() {
        when(userRepository.existsById(99L))
                .thenReturn(false);

        boolean exists = userRepository.existsById(99L);

        assertFalse(exists);
        verify(userRepository, times(1)).existsById(99L);
    }
}