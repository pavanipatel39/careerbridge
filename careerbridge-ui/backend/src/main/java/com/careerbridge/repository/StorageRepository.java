package com.careerbridge.repository;
import java.io.IOException; import java.util.List;
public interface StorageRepository { List<String> readAll() throws IOException; void append(String record) throws IOException; }
