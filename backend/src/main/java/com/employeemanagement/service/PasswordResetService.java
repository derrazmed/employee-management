package com.employeemanagement.service;

import com.employeemanagement.entity.PasswordResetToken;
import com.employeemanagement.entity.User;
import com.employeemanagement.exception.InvalidPasswordResetException;
import com.employeemanagement.repository.PasswordResetTokenRepository;
import com.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int CODE_EXPIRATION_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void requestPasswordReset(String email) {

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return;
        }

        passwordResetTokenRepository
                .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .ifPresent(token -> {
                    token.setUsed(true);
                    passwordResetTokenRepository.save(token);
                });

        String code = generateCode();

        String codeHash = passwordEncoder.encode(code);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .codeHash(codeHash)
                .expiresAt(
                        LocalDateTime.now()
                                .plusMinutes(CODE_EXPIRATION_MINUTES)
                )
                .used(false)
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetCode(
                user.getEmail(),
                code
        );
    }

    @Transactional(noRollbackFor = InvalidPasswordResetException.class)
    public void verifyCode(String email, String code) {

        User user = getUser(email);

        PasswordResetToken resetToken =
                getValidResetToken(user);

        validateCode(resetToken, code);
    }

    @Transactional(noRollbackFor = InvalidPasswordResetException.class)
    public void resetPassword(
            String email,
            String code,
            String newPassword
    ) {

        User user = getUser(email);

        PasswordResetToken resetToken =
                getValidResetToken(user);

        validateCode(resetToken, code);

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        resetToken.setUsed(true);

        passwordResetTokenRepository.save(resetToken);
    }

    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new InvalidPasswordResetException(
                                "Invalid password reset request"
                        )
                );
    }

    private PasswordResetToken getValidResetToken(User user) {

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                        .orElseThrow(() ->
                                new InvalidPasswordResetException(
                                        "Invalid or expired reset code"
                                )
                        );

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {

            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);

            throw new InvalidPasswordResetException(
                    "Invalid or expired reset code"
            );
        }

        if (resetToken.getAttempts() >= MAX_ATTEMPTS) {

            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);

            throw new InvalidPasswordResetException(
                    "Too many verification attempts"
            );
        }

        return resetToken;
    }

    private void validateCode(PasswordResetToken resetToken, String code) {

        if (resetToken.getAttempts() >= MAX_ATTEMPTS) {
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);

            throw new InvalidPasswordResetException(
                    "Invalid or expired reset code"
            );
        }

        boolean matches = passwordEncoder.matches(
                code,
                resetToken.getCodeHash()
        );

        if (!matches) {
            resetToken.setAttempts(resetToken.getAttempts() + 1);

            if (resetToken.getAttempts() >= MAX_ATTEMPTS) {
                resetToken.setUsed(true);
            }

            passwordResetTokenRepository.save(resetToken);

            throw new InvalidPasswordResetException(
                    "Invalid reset code"
            );
        }
    }

    private String generateCode() {

        int number =
                secureRandom.nextInt(1_000_000);

        return String.format(
                "%06d",
                number
        );
    }
}