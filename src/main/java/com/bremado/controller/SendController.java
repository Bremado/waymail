package com.bremado.controller;

import com.bremado.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/email")
@lombok.extern.slf4j.Slf4j
public class SendController {

    private final EmailService emailService;

    public SendController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/donate")
    public ResponseEntity<String> handleDonationWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Received webhook payload: {}", payload);
        
        try {
            // Extract user from 'account_email' or 'email'
            String user = (String) payload.get("account_email");
            if (user == null) {
                user = (String) payload.get("email");
            }
            
            if (user == null) {
                return ResponseEntity.badRequest().body("Field 'account_email' or 'email' is missing in the request body");
            }
            
            emailService.sendDonationEmail(user, payload);
            return ResponseEntity.ok("Email sent successfully");
        } catch (IllegalArgumentException e) {
            log.warn("Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.internalServerError().body("Error sending email: " + e.getMessage());
        }
    }
}
