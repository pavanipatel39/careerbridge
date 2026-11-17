package com.careerbridge.repository;

import com.careerbridge.db.Database;
import com.careerbridge.model.InternshipApplication;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Stores internship applications in the SQL database. */
public class JdbcApplicationRepository implements ApplicationRepository {
    private final Database db;

    public JdbcApplicationRepository(Database db) { this.db = db; }

    @Override
    public void save(InternshipApplication a) throws IOException {
        try (Connection c = db.connection();
             PreparedStatement p = c.prepareStatement("INSERT INTO applications VALUES(?,?,?,?,?)")) {
            p.setString(1, a.getId()); p.setString(2, a.getEmail()); p.setString(3, a.getInternshipId());
            p.setString(4, a.getStatus()); p.setString(5, a.getCreatedAt());
            p.executeUpdate();
        } catch (SQLException e) { throw new IOException("Database error: " + e.getMessage(), e); }
    }

    @Override
    public List<InternshipApplication> findByEmail(String email) throws IOException {
        List<InternshipApplication> out = new ArrayList<>();
        try (Connection c = db.connection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT id,email,internship_id,status,created_at FROM applications WHERE email=? ORDER BY created_at DESC")) {
            p.setString(1, email.toLowerCase());
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) out.add(new InternshipApplication(r.getString(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5)));
            }
        } catch (SQLException e) { throw new IOException("Database error: " + e.getMessage(), e); }
        return out;
    }
}
