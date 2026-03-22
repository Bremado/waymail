package com.bremado.service;

import com.bremado.model.UserEmailConfig;
import com.bremado.repository.UserEmailConfigRepository;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.resend.core.exception.ResendException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@lombok.extern.slf4j.Slf4j
public class EmailService {

    private final UserEmailConfigRepository repository;
    private final Resend resend;

    public EmailService(UserEmailConfigRepository repository, Resend resend) {
        this.repository = repository;
        this.resend = resend;
    }

    public void sendDonationEmail(String user, Map<String, Object> webhookPayload) {
        // 1. Fetch the email configuration for the user
        UserEmailConfig config = repository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Email configuration not found for user: " + user));

        // 2. Determine recipient from payload.payer.email
        String recipient = null;
        if (webhookPayload.containsKey("payer") && webhookPayload.get("payer") instanceof Map) {
            Map<?, ?> payer = (Map<?, ?>) webhookPayload.get("payer");
            recipient = (String) payer.get("email");
        }
        
        if (recipient == null || recipient.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email not found or empty in webhook payload (expected in payer.email)");
        }
        
        recipient = recipient.trim();
        log.info("Preparing to send email to: '{}' using template from user: '{}'", recipient, user);

        // 3. Prepare the email body
        String htmlBody = config.getHtmlBody();
        if (webhookPayload.containsKey("amount")) {
            Object amount = webhookPayload.get("amount");
            if (amount != null) {
                htmlBody = htmlBody.replace("{value}", amount.toString());
            }
        }

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(config.getFromEmail() != null ? config.getFromEmail() : "No Reply <noreply@resend.dev>")
                .to(recipient)
                .subject(config.getSubject())
                .html(htmlBody)
                .build();

        // 4. Send the email
        try {
            CreateEmailResponse data = resend.emails().send(params);
            log.info("Email sent successfully with ID: {}", data.getId());
        } catch (ResendException e) {
            log.error("Failed to send email via Resend. To: {}, From: {}", recipient, config.getFromEmail(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
