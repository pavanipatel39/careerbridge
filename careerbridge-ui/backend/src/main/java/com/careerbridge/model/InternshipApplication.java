package com.careerbridge.model;

import java.time.LocalDateTime;

public class InternshipApplication {
    private final String id, email, internshipId, status, createdAt;

    public InternshipApplication(String id, String email, String internshipId) {
        this(id, email, internshipId, "SUBMITTED", LocalDateTime.now().toString());
    }

    public InternshipApplication(String id, String email, String internshipId, String status, String createdAt) {
        this.id = id; this.email = email; this.internshipId = internshipId; this.status = status; this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getInternshipId() { return internshipId; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }

    public String toCsv() { return id + "\t" + email + "\t" + internshipId + "\t" + status + "\t" + createdAt; }
}
