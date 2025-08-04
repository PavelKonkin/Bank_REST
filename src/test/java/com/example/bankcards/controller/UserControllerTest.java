package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateUserDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("Тесты для UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto userDto1;
    private UserDto userDto2;

    @BeforeEach
    void setUp() {
        userDto1 = new UserDto();
        userDto1.setId(1L);
        userDto1.setUsername("admin");

        userDto2 = new UserDto();
        userDto2.setId(2L);
        userDto2.setUsername("user");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Должен вернуть список всех пользователей для администратора")
    void getAllUsers_whenAdminAccesses_shouldReturnUserList() throws Exception {
        given(userService.findAllUsers()).willReturn(List.of(userDto1, userDto2));

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("admin")))
                .andExpect(jsonPath("$[1].username", is("user")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Должен вернуть пользователя по ID, если он существует")
    void getUserById_whenUserExists_shouldReturnUser() throws Exception {
        given(userService.findUserById(1L)).willReturn(userDto1);

        mockMvc.perform(get("/api/v1/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("admin")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Должен вернуть 404 Not Found, если пользователь по ID не найден")
    void getUserById_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        given(userService.findUserById(anyLong())).willThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/api/v1/admin/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Должен успешно создать нового пользователя")
    void createUser_withValidData_shouldReturnCreated() throws Exception {
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setUsername("newuser");
        createUserDto.setPassword("password123");
        createUserDto.setRole("ROLE_USER");
        doNothing().when(userService).createUser(any(CreateUserDto.class));

        mockMvc.perform(post("/api/v1/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isCreated());

        verify(userService).createUser(any(CreateUserDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Должен вернуть 400 Bad Request при создании пользователя с невалидными данными")
    void createUser_withInvalidData_shouldReturnBadRequest() throws Exception {
        CreateUserDto invalidUserDto = new CreateUserDto();
        invalidUserDto.setUsername("");

        mockMvc.perform(post("/api/v1/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Должен успешно удалить пользователя по ID")
    void deleteUser_whenUserExists_shouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(2L);

        mockMvc.perform(delete("/api/v1/admin/users/2")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(2L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Должен вернуть 403 Forbidden при попытке доступа не администратором")
    void getAllUsers_whenNonAdminAccesses_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Должен вернуть 401 Unauthorized при попытке доступа без аутентификации")
    void getAllUsers_whenNoAuth_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
    }
}