package com.mediahub.subscriptionPlan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediahub.subscriptionPlan.dto.CreateUserRequest;
import com.mediahub.subscriptionPlan.dto.UpdateUserRequest;
import com.mediahub.subscriptionPlan.model.User;
import com.mediahub.subscriptionPlan.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Arrays;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private User user;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();

        user = User.builder()
                .userId(1L)
                .name("Alice Fernandez")
                .roles("Admin")
                .email("alice@mediahub.io")
                .phone("+91-9000000001")
                .country("India")
                .status("Active")
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
    void createUser_Returns201() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(Map.of("message", "User created successfully"));

        mockMvc.perform(post("/mediaHub/subscriptionPlan/users/createUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User created successfully"));
    }

    @Test
    void createUser_DuplicateEmail_Returns409() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new IllegalStateException("Email already exists"));

        mockMvc.perform(post("/mediaHub/subscriptionPlan/users/createUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void fetchUsers_Returns200() throws Exception {
        when(userService.fetchUsers())
                .thenReturn(Arrays.asList(user));

        mockMvc.perform(get("/mediaHub/subscriptionPlan/users/fetchUsers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice Fernandez"))
                .andExpect(jsonPath("$[0].email").value("alice@mediahub.io"));
    }

    @Test
    void fetchUserById_Returns200() throws Exception {
        when(userService.fetchUserById(1L))
                .thenReturn(user);

        mockMvc.perform(get("/mediaHub/subscriptionPlan/users/fetchUser/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("Alice Fernandez"));
    }

    @Test
    void fetchUserById_NotFound_Returns404() throws Exception {
        when(userService.fetchUserById(99L))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/mediaHub/subscriptionPlan/users/fetchUser/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void updateUser_Returns200() throws Exception {
        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class)))
                .thenReturn(Map.of("message", "User updated successfully"));

        mockMvc.perform(put("/mediaHub/subscriptionPlan/users/updateUser/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated successfully"));
    }

    @Test
    void updateUser_NotFound_Returns404() throws Exception {
        when(userService.updateUser(eq(99L), any(UpdateUserRequest.class)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/mediaHub/subscriptionPlan/users/updateUser/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}