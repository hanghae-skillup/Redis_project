package com.example.domain.repository;

import com.example.domain.entity.Users;

import java.util.Optional;

public interface UserRepository {
    Users save(Users users);
    Optional<Users> findById(Long id);
    Optional<Users> findByEmail(String email);
}
