
package com.dm.lib;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class DownloadManager {

    public static final String BUFFER_SIZE_PROP = "buffer.size.property";
    public static final String CONNECTION_TIMEOUT_PROP = "conection.timeout.property";

    private final Thread processThread;
    private final ExecutorService executor;

    private final List<DownloadImpl> active   = new CopyOnWriteArrayList<>();
    private final List<DownloadImpl> finished = new CopyOnWriteArrayList<>();

    private boolean isClosed = false;

    public DownloadManager() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public DownloadManager(int poolSize) {
        executor = Executors.newFixedThreadPool(poolSize);
        processThread = new Thread(processCycle);
    }

    public void start() {
        processThread.start();
    }

    public int getPoolSize() {
        return ((ThreadPoolExecutor) executor).getCorePoolSize();
    }

    public void setPoolSize(int amount) {
        ((ThreadPoolExecutor) executor).setCorePoolSize(amount);
        ((ThreadPoolExecutor) executor).setMaximumPoolSize(amount);
    }

    public List<Download> activeDownloads() {
        return new ArrayList<>(active);
    }

    public List<Download> finishedDownloads() {
        return new ArrayList<>(finished);
    }

    public Download download(URL src, Path dst) throws IOException {
        if (isClosed) {
            throw new IllegalStateException();
        }
        final DownloadImpl download = new DownloadImpl(src, dst, executor);
        active.add(download);
        return download;
    }

    public void close() throws InterruptedException {
        isClosed = true;

        for (Download d : active) {
            d.waitForFinish();
        }
        processThread.interrupt();
        executor.shutdown();
    }

    public void closeForce() {
        isClosed = true;

        processThread.interrupt();
        executor.shutdown();
        for (Download d : active) {
            d.cancel();
        }
    }

    private Runnable processCycle = () -> {
        while (!Thread.currentThread().isInterrupted()) {
            for (DownloadImpl d : active) {
                if (!d.process()) {
                    active.remove(d);
                    finished.add(d);
                }
            }
        }
    };

}
