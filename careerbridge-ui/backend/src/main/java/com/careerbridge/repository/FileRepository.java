package com.careerbridge.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

/** File-based storage. Data directory comes from env DATA_DIR (default: ./data). */
public class FileRepository implements StorageRepository {
    private final Path file;

    public FileRepository(String filename) {
        this(com.careerbridge.util.Config.dataDir(), filename);
    }

    public FileRepository(Path dir, String filename) {
        this.file = dir.resolve(filename);
    }

    @Override
    public synchronized List<String> readAll() throws IOException {
        if (!Files.exists(file)) {
            Files.createDirectories(file.getParent());
            Files.createFile(file);
        }
        return Files.readAllLines(file, StandardCharsets.UTF_8);
    }

    @Override
    public synchronized void append(String record) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, record + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
