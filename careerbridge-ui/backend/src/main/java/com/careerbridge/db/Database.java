package com.careerbridge.db;

import com.careerbridge.util.Config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.List;

/**
 * Database access (H2 file database by default). Override with env DB_URL / DB_USER / DB_PASSWORD
 * to point at any JDBC database (for example a managed cloud database).
 * Default file: <DATA_DIR>/careerbridge.mv.db
 */
public class Database {
    private final String url, user, password;

    public Database() {
        this(defaultUrl(), Config.get("DB_USER", "sa"), Config.get("DB_PASSWORD", ""));
    }

    public Database(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        createSchema();
    }

    public static String defaultUrl() {
        String env = Config.get("DB_URL", "");
        if (!env.isBlank()) return env;
        Path dir = Config.dataDir().toAbsolutePath();
        return "jdbc:h2:file:" + dir.toString().replace('\\', '/') + "/careerbridge;AUTO_SERVER=TRUE";
    }

    public String url() { return url; }

    public Connection connection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public boolean ping() {
        try (Connection c = connection(); Statement s = c.createStatement()) {
            s.execute("SELECT 1");
            return true;
        } catch (SQLException e) { return false; }
    }

    private void createSchema() {
        try (Connection c = connection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(120) NOT NULL, "
                    + "email VARCHAR(255) NOT NULL UNIQUE, password_hash VARCHAR(255) NOT NULL, "
                    + "role VARCHAR(20) NOT NULL, provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            s.execute("CREATE TABLE IF NOT EXISTS applications ("
                    + "id VARCHAR(40) PRIMARY KEY, email VARCHAR(255) NOT NULL, internship_id VARCHAR(60) NOT NULL, "
                    + "status VARCHAR(20) NOT NULL, created_at VARCHAR(40) NOT NULL)");
        } catch (SQLException e) {
            throw new IllegalStateException("Could not open database " + url + ": " + e.getMessage(), e);
        }
        importLegacyFiles();
    }

    /** One-time import of the old users.tsv / applications.tsv records when the tables are empty. */
    private void importLegacyFiles() {
        Path dir = Config.dataDir();
        try (Connection c = connection()) {
            if (count(c, "users") == 0 && Files.exists(dir.resolve("users.tsv"))) {
                for (String line : read(dir.resolve("users.tsv"))) {
                    String[] a = line.split("\\t", -1);
                    if (a.length < 4) continue;
                    try (PreparedStatement p = c.prepareStatement(
                            "INSERT INTO users(name,email,password_hash,role,provider) VALUES(?,?,?,?,'LOCAL')")) {
                        p.setString(1, a[0]); p.setString(2, a[1].toLowerCase()); p.setString(3, a[2]); p.setString(4, a[3]);
                        p.executeUpdate();
                    } catch (SQLException ignored) { /* duplicate or bad row: skip */ }
                }
            }
            if (count(c, "applications") == 0 && Files.exists(dir.resolve("applications.tsv"))) {
                for (String line : read(dir.resolve("applications.tsv"))) {
                    String[] a = line.split("\\t", -1);
                    if (a.length < 5) continue;
                    try (PreparedStatement p = c.prepareStatement("INSERT INTO applications VALUES(?,?,?,?,?)")) {
                        p.setString(1, a[0]); p.setString(2, a[1].toLowerCase()); p.setString(3, a[2]);
                        p.setString(4, a[3]); p.setString(5, a[4]);
                        p.executeUpdate();
                    } catch (SQLException ignored) { }
                }
            }
        } catch (SQLException | IOException e) {
            System.err.println("Legacy import skipped: " + e.getMessage());
        }
    }

    private static List<String> read(Path p) throws IOException { return Files.readAllLines(p, StandardCharsets.UTF_8); }

    private static int count(Connection c, String table) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            r.next();
            return r.getInt(1);
        }
    }
}
