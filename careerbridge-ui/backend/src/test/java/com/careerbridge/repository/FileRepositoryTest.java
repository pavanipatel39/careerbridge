package com.careerbridge.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileRepositoryTest {
    @TempDir Path dir;

    @Test void readingMissingFileCreatesItAndReturnsEmptyList() throws Exception {
        assertTrue(new FileRepository(dir.resolve("sub"), "x.log").readAll().isEmpty());
        assertTrue(java.nio.file.Files.exists(dir.resolve("sub").resolve("x.log")));
    }

    @Test void appendedLinesAreReadBackInOrder() throws Exception {
        FileRepository r = new FileRepository(dir, "x.log");
        r.append("one"); r.append("two");
        assertEquals(List.of("one", "two"), r.readAll());
    }

    @Test void dataSurvivesANewRepositoryInstance() throws Exception {
        new FileRepository(dir, "x.log").append("kept");
        assertEquals(List.of("kept"), new FileRepository(dir, "x.log").readAll());
    }

    @Test void concurrentAppendsDoNotLoseLines() throws Exception {
        FileRepository r = new FileRepository(dir, "x.log");
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 100; i++) { int n = i; pool.submit(() -> { try { r.append("line" + n); } catch (Exception e) { throw new RuntimeException(e); } }); }
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(100, r.readAll().size());
    }
}
