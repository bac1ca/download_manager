
package com.dm.lib;


import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DManager {

    private final List<Download> active = new CopyOnWriteArrayList<>();
    private final List<Download> finished = new CopyOnWriteArrayList<>();

    private final ExecutorService executor;
    private final Thread processThread;


    public DManager() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public DManager(int threadPoolSize) {
        executor = Executors.newFixedThreadPool(threadPoolSize);
        processThread = new Thread(processCycle);
    }

    public void start() {
        processThread.start();
    }

    public int getThreadPoolSize() {
        return ((ThreadPoolExecutor) executor).getCorePoolSize();
    }

    public void setThreadPoolSize(int amount) {
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
        final Download download = new Download(src, dst, executor);
        active.add(download);
        return download;
    }

    public void close() {
        executor.shutdown();
        processThread.interrupt();
    }

    private Runnable processCycle = new Runnable() {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                for (Download d : new ArrayList<>(active)) {
                    if (!d.process()) {
                        active.remove(d);
                        finished.add(d);
                    }
                }
            }
        }
    };


    private static DJobBase.StatusListener listener = new DJobBase.StatusListener() {
        @Override
        public void notify(DJobBase.Status status, double progress) {
            System.out.println("status = " + status);
            System.out.println("progress = " + progress);
        }
    };

    public static void main(String[] args) throws Exception {
        String addr = "http://localhost:8000/RangeHTTPServer.py";
        URL src = new URL(addr);
        Path dst = Paths.get("./ServerHTTP2.py");

        DManager dm = new DManager();
        try {
            dm.start();
            Download download = dm.download(src, dst);
            download.setStatusListener(listener);

            while (true) {
                double progress = download.getProgress();
//                System.out.println("progress = " + progress);
                if (progress == 1.0) {
                    Thread.sleep(100);
                    break;
                }
            }

        } finally {
            dm.close();
        }
    }

}
