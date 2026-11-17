package com.careerbridge.service;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class BackgroundProcessorTest {
    @Test void closeWaitsForSubmittedTasks() {
        AtomicInteger done = new AtomicInteger();
        BackgroundProcessor p = new BackgroundProcessor();
        for (int i = 0; i < 20; i++) p.submit(() -> {
            try { Thread.sleep(20); } catch (InterruptedException ignored) { }
            done.incrementAndGet();
        });
        p.close();
        assertEquals(20, done.get());
    }

    @Test void tasksRunOnWorkerThreadsNotTheCaller() {
        var name = new java.util.concurrent.atomic.AtomicReference<String>();
        BackgroundProcessor p = new BackgroundProcessor();
        p.submit(() -> name.set(Thread.currentThread().getName()));
        p.close();
        assertNotEquals(Thread.currentThread().getName(), name.get());
    }
}
