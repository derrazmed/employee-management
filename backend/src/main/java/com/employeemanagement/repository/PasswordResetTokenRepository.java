package com.employeemanagement.repository;

import com.employeemanagement.entity.PasswordResetToken;
import com.employeemanagement.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PasswordResetToken>
    findTopByUserAndUsedFalseOrderByCreatedAtDesc(User user);
}