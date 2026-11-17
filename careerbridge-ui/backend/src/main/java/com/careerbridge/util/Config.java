package com.careerbridge.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/** Reads settings from -D system properties first, then environment variables, then defaults. */
public final class Config {
    private Config() { }

    public static String get(String key, String fallback) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) v = System.getenv(key);
        return (v == null || v.isBlank()) ? fallback : v;
    }

    public static Path dataDir() { return Paths.get(get("DATA_DIR", "data")); }
}
