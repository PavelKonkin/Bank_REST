package com.example.bankcards.service;

import com.example.bankcards.dto.CreateUserDto;
import com.example.bankcards.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> findAllUsers();

    UserDto findUserById(Long id);

    void createUser(CreateUserDto user);

    void deleteUser(Long id);
}
