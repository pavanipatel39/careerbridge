package com.careerbridge.repository;

import com.careerbridge.db.Database;
import com.careerbridge.exception.DuplicateUserException;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

/** Stores users in the SQL database. */
public class JdbcUserRepository implements UserRepository {
    private final Database db;

    public JdbcUserRepository(Database db) { this.db = db; }

    @Override
    public Optional<UserRecord> findByEmail(String email) throws IOException {
        try (Connection c = db.connection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT name,email,password_hash,role,provider FROM users WHERE email=?")) {
            p.setString(1, email.toLowerCase());
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return Optional.empty();
                return Optional.of(new UserRecord(r.getString(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5)));
            }
        } catch (SQLException e) { throw new IOException("Database error: " + e.getMessage(), e); }
    }

    @Override
    public void save(UserRecord u) throws DuplicateUserException, IOException {
        try (Connection c = db.connection();
             PreparedStatement p = c.prepareStatement(
                     "INSERT INTO users(name,email,password_hash,role,provider) VALUES(?,?,?,?,?)")) {
            p.setString(1, u.name()); p.setString(2, u.email().toLowerCase()); p.setString(3, u.passwordHash());
            p.setString(4, u.role()); p.setString(5, u.provider());
            p.executeUpdate();
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) throw new DuplicateUserException("An account with this email already exists.");
            throw new IOException("Database error: " + e.getMessage(), e);
        }
    }
}
