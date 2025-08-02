package com.example.bankcards.util;

import com.example.bankcards.dto.CreateUserDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "role", target = "role", qualifiedByName = "roleEntityToRoleName")
    UserDto toDto(User user);

    @Mapping(source = "role", target = "role", qualifiedByName = "roleNameToRoleEntity")
    @Mapping(source = "password", target = "password", qualifiedByName = "encodePassword")
    User toEntity(CreateUserDto createUserDto, @Context PasswordEncoder passwordEncoder);

    @Named("roleEntityToRoleName")
    default String roleEntityToRoleName(UserRole role) {
        if (role == null) {
            return null;
        }
        return role.name();
    }

    @Named("roleNameToRoleEntity")
    default UserRole roleNameToRoleEntity(String roleName) {
        if (roleName == null) {
            return UserRole.ROLE_USER;
        }
        return UserRole.valueOf(roleName);
    }

    @Named("encodePassword")
    default String encodePassword(String password, @Context PasswordEncoder passwordEncoder) {
        if (password == null || passwordEncoder == null) {
            return null;
        }
        return passwordEncoder.encode(password);
    }
}
