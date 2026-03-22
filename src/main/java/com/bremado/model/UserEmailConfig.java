package com.bremado.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "email_configs")
public class UserEmailConfig {
    @Id
    private String id;
    private String user; // The identifier used in the route /email/donate/:user
    private String subject;
    private String htmlBody;
    private String fromEmail; // e.g. "Onboarding <onboarding@resend.dev>"
}
