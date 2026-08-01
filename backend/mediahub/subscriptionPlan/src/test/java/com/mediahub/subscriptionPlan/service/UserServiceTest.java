package com.mediahub.subscriptionPlan.service;

import com.mediahub.subscriptionPlan.dto.CreateUserRequest;
import com.mediahub.subscriptionPlan.dto.UpdateUserRequest;
import com.mediahub.subscriptionPlan.model.User;
import com.mediahub.subscriptionPlan.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L).name("Alice Fernandez").roles("Admin")
                .email("alice@mediahub.io").phone("+91-9000000001")
                .country("India").status("Active")
                .build();

        createRequest = new CreateUserRequest();
        createRequest.setName("Alice Fernandez");
        createRequest.setRoles("Admin");
        createRequest.setEmail("alice@mediahub.io");
        createRequest.setPhone("+91-9000000001");
        createRequest.setCountry("India");

        updateRequest = new UpdateUserRequest();
        updateRequest.setName("Alice F");
        updateRequest.setPhone("+91-9000000099");
        updateRequest.setCountry("USA");
    }

    @Test
    void createUser_Success() {
        when(userRepository.findByEmail("alice@mediahub.io"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        Map<String, String> result = userService.createUser(createRequest);

        assertEquals("User created successfully", result.get("message"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyExists_ThrowsException() {
        when(userRepository.findByEmail("alice@mediahub.io"))
                .thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class,
                () -> userService.createUser(createRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void fetchUsers_Success() {
        when(userRepository.findAll())
                .thenReturn(Arrays.asList(user));

        List<User> result = userService.fetchUsers();

        assertEquals(1, result.size());
        assertEquals("Alice Fernandez", result.get(0).getName());
    }

    @Test
    void fetchUserById_Success() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        User result = userService.fetchUserById(1L);

        assertEquals(1L, result.getUserId());
        assertEquals("alice@mediahub.io", result.getEmail());
        assertEquals("Admin", result.getRoles());
    }

    @Test
    void fetchUserById_NotFound_ThrowsException() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.fetchUserById(99L));
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        Map<String, String> result = userService.updateUser(1L, updateRequest);

        assertEquals("User updated successfully", result.get("message"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_NotFound_ThrowsException() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.updateUser(99L, updateRequest));
    }
}