package com.example.eventmateai.repository;

import com.example.eventmateai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // kept for backward compat if needed, but risky if multiple exist
    Optional<User> findByEmailAndRole(String email, String role);
    java.util.List<User> findAllByEmail(String email);

}
