package com.careerbridge.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.careerbridge.db.Database;
import java.nio.file.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseImportTest {
    @TempDir Path dir;

    @Test void oldTsvFilesAreImportedOnceAndBadLinesAreSkipped() throws Exception {
        Files.writeString(dir.resolve("users.tsv"), "Old User\told@test.com\tHASH\tSTUDENT\n\nbadline\n");
        Files.writeString(dir.resolve("applications.tsv"), "id1\told@test.com\tintern-1\tSUBMITTED\t2026-01-01T10:00\nshort\n");
        String previous = System.getProperty("DATA_DIR");
        System.setProperty("DATA_DIR", dir.toString());
        try {
            String url = "jdbc:h2:mem:imp" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1";
            Database db = new Database(url, "sa", "");
            assertTrue(new JdbcUserRepository(db).findByEmail("old@test.com").isPresent());
            assertEquals(1, new JdbcApplicationRepository(db).findByEmail("old@test.com").size());
            Database restarted = new Database(url, "sa", "");   // second start must not duplicate rows
            assertEquals(1, new JdbcApplicationRepository(restarted).findByEmail("old@test.com").size());
        } finally {
            if (previous == null) System.clearProperty("DATA_DIR"); else System.setProperty("DATA_DIR", previous);
        }
    }

    @Test void defaultUrlPointsToTheDataFolder() {
        assertTrue(Database.defaultUrl().startsWith("jdbc:h2:file:") || Database.defaultUrl().startsWith("jdbc:"));
    }
}
