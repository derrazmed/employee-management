package com.employeemanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetCode(
            String recipientEmail,
            String code
    ) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(recipientEmail);
        message.setSubject("Password Reset Code");
        message.setText(
                "Hello,\n\n"
                        + "We received a request to reset your password.\n\n"
                        + "Your password reset code is:\n\n"
                        + code
                        + "\n\n"
                        + "This code will expire in 10 minutes.\n\n"
                        + "If you did not request a password reset, "
                        + "you can safely ignore this email.\n\n"
                        + "Regards,\n"
                        + "Employee Management"
        );

        mailSender.send(message);
    }
}