package com.careerbridge.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.careerbridge.db.Database;
import com.careerbridge.exception.DuplicateUserException;
import com.careerbridge.model.InternshipApplication;
import com.careerbridge.repository.UserRepository.UserRecord;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcRepositoryTest {
    Database db;
    JdbcUserRepository users;
    JdbcApplicationRepository apps;

    @BeforeEach void setUp() {
        db = new Database("jdbc:h2:mem:t" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1", "sa", "");
        users = new JdbcUserRepository(db);
        apps = new JdbcApplicationRepository(db);
    }

    @Test void pingWorks() { assertTrue(db.ping()); }

    @Test void savedUserCanBeFound() throws Exception {
        users.save(new UserRecord("Riya", "riya@test.com", "hash", "STUDENT", "LOCAL"));
        UserRecord u = users.findByEmail("riya@test.com").orElseThrow();
        assertEquals("Riya", u.name());
        assertEquals("LOCAL", u.provider());
    }

    @Test void findIsCaseInsensitive() throws Exception {
        users.save(new UserRecord("Riya", "riya@test.com", "hash", "STUDENT", "LOCAL"));
        assertTrue(users.findByEmail("RIYA@TEST.COM").isPresent());
    }

    @Test void unknownUserIsEmpty() throws Exception { assertTrue(users.findByEmail("no@test.com").isEmpty()); }

    @Test void duplicateEmailViolatesUniqueConstraint() throws Exception {
        users.save(new UserRecord("A", "a@test.com", "h", "STUDENT", "LOCAL"));
        assertThrows(DuplicateUserException.class, () -> users.save(new UserRecord("B", "A@test.com", "h", "STUDENT", "LOCAL")));
    }

    @Test void applicationsAreSavedAndFilteredByEmail() throws Exception {
        apps.save(new InternshipApplication("1", "a@test.com", "intern-1"));
        apps.save(new InternshipApplication("2", "a@test.com", "intern-2"));
        apps.save(new InternshipApplication("3", "b@test.com", "intern-1"));
        assertEquals(2, apps.findByEmail("a@test.com").size());
        assertEquals(1, apps.findByEmail("B@test.com").size());
        assertTrue(apps.findByEmail("none@test.com").isEmpty());
    }

    @Test void sqlInjectionTextIsTreatedAsPlainData() throws Exception {
        String evil = "x@test.com' OR '1'='1";
        users.save(new UserRecord("Evil", evil, "h", "STUDENT", "LOCAL"));
        users.save(new UserRecord("Normal", "n@test.com", "h", "STUDENT", "LOCAL"));
        assertEquals("Evil", users.findByEmail(evil).orElseThrow().name());
        assertTrue(users.findByEmail("' OR '1'='1").isEmpty());
    }

    @Test void applicationListIsNewestFirst() throws Exception {
        apps.save(new InternshipApplication("1", "a@test.com", "i1", "SUBMITTED", "2026-01-01T10:00"));
        apps.save(new InternshipApplication("2", "a@test.com", "i2", "SUBMITTED", "2026-02-01T10:00"));
        List<InternshipApplication> l = apps.findByEmail("a@test.com");
        assertEquals("2", l.get(0).getId());
    }
}
