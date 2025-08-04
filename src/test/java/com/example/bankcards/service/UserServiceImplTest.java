package com.example.bankcards.service;

import com.example.bankcards.dto.CreateUserDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.UserServiceImpl;
import com.example.bankcards.util.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user1;
    private UserDto userDto1;
    private CreateUserDto createUserDto;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setPassword("encodedPassword");

        userDto1 = new UserDto();
        userDto1.setId(1L);

        createUserDto = new CreateUserDto();
        createUserDto.setUsername("newuser");
        createUserDto.setPassword("rawPassword");
        createUserDto.setRole("ROLE_USER");
    }

    @Test
    @DisplayName("findAllUsers должен возвращать список UserDto, когда пользователи существуют")
    void findAllUsers_whenUsersExist_shouldReturnUserDtoList() {
        List<User> users = List.of(user1);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDto(user1)).thenReturn(userDto1);

        List<UserDto> result = userService.findAllUsers();

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo(userDto1.getUsername());
        verify(userRepository).findAll();
        verify(userMapper).toDto(any(User.class));
    }

    @Test
    @DisplayName("findAllUsers должен возвращать пустой список, когда пользователей нет")
    void findAllUsers_whenNoUsersExist_shouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.findAllUsers();

        assertThat(result).isNotNull().isEmpty();
        verify(userRepository).findAll();
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("findUserById должен возвращать UserDto, если пользователь найден")
    void findUserById_whenUserExists_shouldReturnUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userMapper.toDto(user1)).thenReturn(userDto1);

        UserDto result = userService.findUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("findUserById должен бросать NotFoundException, если пользователь не найден")
    void findUserById_whenUserDoesNotExist_shouldThrowNotFoundException() {
        long nonExistentId = 99L;
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.findUserById(nonExistentId)
        );

        assertThat(exception.getMessage()).isEqualTo("User not found");
        verify(userRepository).findById(nonExistentId);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("createUser должен успешно сохранять нового пользователя")
    void createUser_shouldSaveNewUser() {
        when(userMapper.toEntity(any(CreateUserDto.class), any(PasswordEncoder.class)))
                .thenReturn(user1);

        when(userRepository.save(user1)).thenReturn(user1);

        userService.createUser(createUserDto);

        verify(userRepository, times(1)).save(user1);
        verify(userMapper).toEntity(createUserDto, passwordEncoder);
    }

    @Test
    @DisplayName("deleteUser должен вызывать deleteById у репозитория")
    void deleteUser_shouldCallDeleteById() {
        long userId = 1L;
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }
}
