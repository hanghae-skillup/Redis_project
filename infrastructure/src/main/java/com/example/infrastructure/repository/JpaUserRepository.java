
package com.example.infrastructure.repository;

import com.example.domain.entity.Users;
import com.example.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<Users, Long>, UserRepository {
    @Override
    Optional<Users> findByEmail(String email);
}
