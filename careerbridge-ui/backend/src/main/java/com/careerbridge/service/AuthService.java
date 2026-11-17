package com.careerbridge.service;

import com.careerbridge.exception.*;
import com.careerbridge.model.Student;
import com.careerbridge.repository.UserRepository;
import com.careerbridge.repository.UserRepository.UserRecord;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

/** Registration and login. Passwords stored as salted PBKDF2 (legacy SHA-256 hashes still accepted). */
public class AuthService {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int ITERATIONS = 120_000;
    static final String NO_PASSWORD = "GOOGLE-ACCOUNT-NO-PASSWORD";
    private final UserRepository users;

    public AuthService(UserRepository users) { this.users = users; }

    public synchronized Student register(String name, String email, String password)
            throws InvalidInputException, DuplicateUserException, IOException {
        name = clean(name);
        email = clean(email).toLowerCase();
        if (name.isBlank() || !EMAIL.matcher(email).matches() || password == null || password.length() < 6)
            throw new InvalidInputException("Enter a valid name, email and password (minimum 6 characters).");
        if (users.findByEmail(email).isPresent())
            throw new DuplicateUserException("An account with this email already exists.");
        String hash = hashPassword(password);
        users.save(new UserRecord(name, email, hash, "STUDENT", "LOCAL"));
        return new Student(name, email, hash);
    }

    public synchronized Student login(String email, String password) throws InvalidCredentialsException, IOException {
        Optional<UserRecord> u = users.findByEmail(clean(email).toLowerCase());
        if (u.isEmpty() || password == null || !verify(password, u.get().passwordHash()))
            throw new InvalidCredentialsException("Invalid email or password.");
        return new Student(u.get().name(), u.get().email(), u.get().passwordHash());
    }

    /** Sign in (or create the account on first use) for an already-verified Google profile. */
    public synchronized Student loginWithGoogle(String email, String name) throws IOException, InvalidInputException {
        String mail = clean(email).toLowerCase();
        if (!EMAIL.matcher(mail).matches()) throw new InvalidInputException("Google account has no valid email.");
        try {
            Optional<UserRecord> u = users.findByEmail(mail);
            if (u.isPresent()) return new Student(u.get().name(), u.get().email(), u.get().passwordHash());
            String display = clean(name).isBlank() ? mail : clean(name);
            users.save(new UserRecord(display, mail, NO_PASSWORD, "STUDENT", "GOOGLE"));
            return new Student(display, mail, NO_PASSWORD);
        } catch (DuplicateUserException e) {
            throw new IOException("Concurrent sign-in, please retry", e);
        }
    }

    private static String clean(String s) { return s == null ? "" : s.replaceAll("[\\t\\r\\n]", " ").trim(); }

    static String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(pbkdf2(password, salt, ITERATIONS));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    static boolean verify(String password, String stored) {
        try {
            if (stored.startsWith("pbkdf2$")) {
                String[] p = stored.split("\\$");
                byte[] expected = Base64.getDecoder().decode(p[3]);
                byte[] actual = pbkdf2(password, Base64.getDecoder().decode(p[2]), Integer.parseInt(p[1]));
                return MessageDigest.isEqual(expected, actual);
            }
            if (NO_PASSWORD.equals(stored)) return false;
            // legacy unsalted SHA-256 (accounts created before the security fix)
            byte[] legacy = MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(Base64.getEncoder().encode(legacy), stored.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) { return false; }
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    }
}
