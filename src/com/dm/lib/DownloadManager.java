
package com.dm.lib;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


/**
 * This class represents DownloadManager entity, which can be run and stopped (soft
 * and hard). Also we can create {@see Download} objects using {@see download}
 * method. It provides list with running "Download" tasks and list of already
 * finished tasks.
 */
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

    /**
     * Creates DownloadManager with fixed number of worker threads,
     * the minimum number is 1, the maximum number is 256,
     * @param poolSize the number of worker threads
     * @throws IllegalArgumentException if pool-size is out of range [1, 256]
     */
    public DownloadManager(int poolSize) {
        if (poolSize < 1 || poolSize > 256) {
            throw new IllegalArgumentException("pool size < 1");
        }
        executor = Executors.newFixedThreadPool(poolSize);
        processThread = new Thread(processCycle);
    }

    /**
     * Runs DownloadManager,
     * e.g. starts "process" thread
     */
    public void start() {
        processThread.start();
    }

    /**
     * Returns current number of worker threads
     * @return current number of worker threads
     */
    public int getPoolSize() {
        return ((ThreadPoolExecutor) executor).getCorePoolSize();
    }

    /**
     * Sets the number of worker threads,
     * the minimum number is 1, the maximum number is 256,
     * this method overrides the initial number of threads
     * @param poolSize the number of worker threads
     * @throws IllegalArgumentException if pool-size is out of range [1, 256]
     */
    public void setPoolSize(int poolSize) {
        if (poolSize < 1 || poolSize > 256) {
            throw new IllegalArgumentException("pool size < 1");
        }
        ((ThreadPoolExecutor) executor).setCorePoolSize(poolSize);
        ((ThreadPoolExecutor) executor).setMaximumPoolSize(poolSize);
    }

    /**
     * Returns list of active download tasks
     * @return list of active download tasks
     */
    public List<Download> activeDownloads() {
        return new ArrayList<>(active);
    }

    /**
     * Returns list of finished download tasks
     * @return list of finished download tasks
     */
    public List<Download> finishedDownloads() {
        return new ArrayList<>(finished);
    }

    /**
     * Creates and run new {@see Download} tasks
     * @param src the source URL
     * @param dst the destination path
     * @return Download object
     * @throws IOException if any IO error occurs
     */
    public Download download(URL src, Path dst) throws IOException {
        if (isClosed) {
            throw new IllegalStateException();
        }
        final DownloadImpl download = new DownloadImpl(src, dst, executor);
        active.add(download);
        return download;
    }

    /**
     * Graceful shutdown of DownloadManager,
     * It blocks until all downloads are finished
     * @throws InterruptedException
     */
    public void close() throws InterruptedException {
        isClosed = true;

        for (Download d : active) {
            d.waitForFinish();
        }
        processThread.interrupt();
        executor.shutdown();
    }

    /**
     * Force close of DownloadManager,
     * cancels all active tasks
     */
    public void closeForce() {
        isClosed = true;

        for (Download d : active) {
            d.cancel();
        }
        processThread.interrupt();
        executor.shutdown();
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
