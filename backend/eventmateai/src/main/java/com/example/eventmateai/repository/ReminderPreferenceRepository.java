package com.example.eventmateai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.eventmateai.model.ReminderPreference;

public interface ReminderPreferenceRepository extends JpaRepository<ReminderPreference, Long> {

    Optional<ReminderPreference> findByUserId(Long userId);
}
