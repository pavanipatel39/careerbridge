package com.careerbridge.service;

import java.util.concurrent.*;

/** Thread pool that saves submitted applications in the background. */
public class BackgroundProcessor implements AutoCloseable {
    private final ExecutorService workers = Executors.newFixedThreadPool(3);

    public void submit(Runnable task) { workers.submit(task); }

    @Override
    public void close() {
        workers.shutdown();
        try { workers.awaitTermination(5, TimeUnit.SECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
