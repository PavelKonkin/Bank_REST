package com.example.bankcards.dto;

import lombok.Data;

@Data
public class CreateUserDto {
    private Long id;
    private String username;
    private String password;
    private String role;
}
