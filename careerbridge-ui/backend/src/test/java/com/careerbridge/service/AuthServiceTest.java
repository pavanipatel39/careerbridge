package com.careerbridge.service;

import static org.junit.jupiter.api.Assertions.*;

import com.careerbridge.exception.*;
import com.careerbridge.model.Student;
import com.careerbridge.repository.UserRepository;
import com.careerbridge.repository.UserRepository.UserRecord;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AuthServiceTest {
    /** Simple in-memory repository so these are true unit tests (no database). */
    static class FakeRepo implements UserRepository {
        final Map<String, UserRecord> rows = new ConcurrentHashMap<>();
        public Optional<UserRecord> findByEmail(String e) { return Optional.ofNullable(rows.get(e.toLowerCase())); }
        public void save(UserRecord u) throws DuplicateUserException {
            if (rows.putIfAbsent(u.email().toLowerCase(), u) != null) throw new DuplicateUserException("dup");
        }
    }

    FakeRepo repo;
    AuthService auth;

    @BeforeEach void setUp() { repo = new FakeRepo(); auth = new AuthService(repo); }

    @Test void registerValidUser() throws Exception {
        Student s = auth.register("Riya Shah", "riya@test.com", "secret123");
        assertEquals("riya@test.com", s.getEmail());
        assertEquals(1, repo.rows.size());
    }

    @Test void emailIsTrimmedAndLowerCased() throws Exception {
        auth.register("Riya", "  Riya@Test.COM ", "secret123");
        assertTrue(repo.rows.containsKey("riya@test.com"));
    }

    @Test void tabsAndNewlinesAreRemovedFromName() throws Exception {
        Student s = auth.register("Ri\tya\nShah", "r@test.com", "secret123");
        assertFalse(s.getName().contains("\t") || s.getName().contains("\n"));
    }

    @ParameterizedTest
    @CsvSource({"'',a@b.com,secret123", "Riya,not-an-email,secret123", "Riya,a@b,secret123", "Riya,a@b.com,12345", "Riya,a@b.com,''"})
    void invalidRegistrationIsRejected(String name, String email, String pass) {
        assertThrows(InvalidInputException.class, () -> auth.register(name, email, pass));
    }

    @Test void passwordOfExactlySixCharactersIsAccepted() throws Exception {
        assertNotNull(auth.register("Riya", "six@test.com", "abcdef"));
    }

    @Test void nullValuesAreRejected() {
        assertThrows(InvalidInputException.class, () -> auth.register(null, null, null));
    }

    @Test void duplicateEmailIsRejectedCaseInsensitively() throws Exception {
        auth.register("Riya", "riya@test.com", "secret123");
        assertThrows(DuplicateUserException.class, () -> auth.register("Other", "RIYA@test.com", "secret123"));
    }

    @Test void loginWithCorrectPassword() throws Exception {
        auth.register("Riya", "riya@test.com", "secret123");
        assertEquals("Riya", auth.login("riya@test.com", "secret123").getName());
    }

    @Test void loginIsCaseInsensitiveForEmail() throws Exception {
        auth.register("Riya", "riya@test.com", "secret123");
        assertNotNull(auth.login("RIYA@Test.com", "secret123"));
    }

    @Test void loginWithWrongPasswordFails() throws Exception {
        auth.register("Riya", "riya@test.com", "secret123");
        assertThrows(InvalidCredentialsException.class, () -> auth.login("riya@test.com", "wrong"));
    }

    @Test void loginWithUnknownEmailFails() {
        assertThrows(InvalidCredentialsException.class, () -> auth.login("nobody@test.com", "secret123"));
    }

    @Test void loginWithNullPasswordFails() throws Exception {
        auth.register("Riya", "riya@test.com", "secret123");
        assertThrows(InvalidCredentialsException.class, () -> auth.login("riya@test.com", null));
    }

    @Test void passwordsAreStoredSaltedSoSamePasswordGivesDifferentHashes() throws Exception {
        auth.register("A", "a@test.com", "samepass1");
        auth.register("B", "b@test.com", "samepass1");
        assertNotEquals(repo.rows.get("a@test.com").passwordHash(), repo.rows.get("b@test.com").passwordHash());
        assertTrue(repo.rows.get("a@test.com").passwordHash().startsWith("pbkdf2$"));
    }

    @Test void plainPasswordIsNeverStored() throws Exception {
        auth.register("A", "a@test.com", "secret123");
        assertFalse(repo.rows.get("a@test.com").passwordHash().contains("secret123"));
    }

    @Test void legacyUnsaltedSha256AccountsCanStillLogIn() throws Exception {
        byte[] d = MessageDigest.getInstance("SHA-256").digest("oldpass1".getBytes(StandardCharsets.UTF_8));
        repo.save(new UserRecord("Old", "old@test.com", Base64.getEncoder().encodeToString(d), "STUDENT", "LOCAL"));
        assertEquals("Old", auth.login("old@test.com", "oldpass1").getName());
        assertThrows(InvalidCredentialsException.class, () -> auth.login("old@test.com", "nope"));
    }

    @Test void googleLoginCreatesAccountOnFirstUseAndReusesIt() throws Exception {
        Student first = auth.loginWithGoogle("G@Gmail.com", "Gita");
        Student second = auth.loginWithGoogle("g@gmail.com", "Other Name");
        assertEquals("Gita", first.getName());
        assertEquals("Gita", second.getName());
        assertEquals(1, repo.rows.size());
        assertEquals("GOOGLE", repo.rows.get("g@gmail.com").provider());
    }

    @Test void googleAccountCannotLogInWithAPassword() throws Exception {
        auth.loginWithGoogle("g@gmail.com", "Gita");
        assertThrows(InvalidCredentialsException.class, () -> auth.login("g@gmail.com", AuthService.NO_PASSWORD));
        assertThrows(InvalidCredentialsException.class, () -> auth.login("g@gmail.com", "anything1"));
    }

    @Test void googleLoginRejectsBadEmail() {
        assertThrows(InvalidInputException.class, () -> auth.loginWithGoogle("bad", "X"));
    }

    @Test void concurrentRegistrationOfSameEmailCreatesOnlyOneAccount() throws Exception {
        int n = 20;
        var pool = java.util.concurrent.Executors.newFixedThreadPool(n);
        var ok = new java.util.concurrent.atomic.AtomicInteger();
        var latch = new java.util.concurrent.CountDownLatch(n);
        for (int i = 0; i < n; i++) pool.submit(() -> {
            try { auth.register("Same", "same@test.com", "secret123"); ok.incrementAndGet(); }
            catch (DuplicateUserException | InvalidInputException | IOException ignored) { }
            latch.countDown();
        });
        latch.await();
        pool.shutdown();
        assertEquals(1, ok.get());
    }
}
