package com.careerbridge.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ModelTest {
    @Test void studentHasStudentRole() { assertEquals("STUDENT", new Student("A", "a@x.com", "h").getRole()); }

    @Test void polymorphismGivesDifferentDashboards() {
        User student = new Student("A", "a@x.com", "h");
        User employer = new Employer("B", "b@x.com", "h");
        assertNotEquals(student.getDashboardType(), employer.getDashboardType());
    }

    @Test void applicationDefaultsToSubmitted() {
        InternshipApplication a = new InternshipApplication("1", "a@x.com", "intern-1");
        assertEquals("SUBMITTED", a.getStatus());
        assertFalse(a.getCreatedAt().isBlank());
    }

    @Test void applicationCsvHasFiveTabSeparatedColumns() {
        InternshipApplication a = new InternshipApplication("1", "a@x.com", "intern-1", "SUBMITTED", "t");
        assertEquals(5, a.toCsv().split("\t").length);
    }
}
