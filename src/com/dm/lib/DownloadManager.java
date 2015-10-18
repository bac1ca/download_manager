
package com.dm.lib;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import com.dm.lib.DJobBase.State;
import com.dm.lib.DJobBase.Status;
import com.dm.lib.DJobBase.StatusListener;

public class DownloadManager {

    private final List<Download> active   = new CopyOnWriteArrayList<>();
    private final List<Download> finished = new CopyOnWriteArrayList<>();

    private final ExecutorService executor;
    private final Thread processThread;

    public DownloadManager() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public DownloadManager(int threadPoolSize) {
        executor = Executors.newFixedThreadPool(threadPoolSize);
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
        final Download download = new Download(src, dst, executor);
        active.add(download);
        return download;
    }

    public void close() {
        processThread.interrupt();
        executor.shutdown();
    }

    private Runnable processCycle = new Runnable() {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                for (Download d : active) {
                    if (!d.process()) {
                        active.remove(d);
                        finished.add(d);
                    }
                }
            }
        }
    };


    public static void main(String[] args) {
        // for tests only
        args = new String [] {
                "http://localhost:8000/AlgorithmsinJava21.pdf",
                "http://localhost:8001/AlgorithmsinJava2.pdf",
                "./downloads/"
        };


        DownloadManager dm = new DownloadManager();
        try {
            if (args.length < 2) {
                printHelp();
                return;
            }
            final String out = args[args.length - 1];
            final Path dst = Paths.get(out);
            if (!Files.exists(dst) || !Files.isDirectory(dst)) {
                System.out.println("Destination path is incorrect: " + out);
                return;
            }

            dm.start();
            List<Download> downloads = new ArrayList<>();
            for (int i = 0; i < (args.length - 1); i++) {

                // prepare URL
                final URL url;
                try {
                    url = new URL(args[i]);
                } catch (MalformedURLException e) {
                    System.err.println();
                    return;
                }

                // prepare destination file
                String name = url.getFile();
                name = name.substring(name.lastIndexOf('/') + 1);

                // run downloading
                try {
                    System.out.println("Starting downloading: " + url);
                    final Download download = dm.download(url, Paths.get(out, name));
                    download.setStatusListener((Status s, double progress) -> {
                        if (s.getState() != State.INPROGRESS) {
                            System.out.println("finished: " + download);
                        }
                    });
                    downloads.add(download);
                } catch (IOException e) {
                    System.err.println("IOE while downloading: " + url +
                            ", " + e.getMessage());
                }
            }

            // wait for finish all downloads
            for (Download d : downloads) {
                d.waitForFinish();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            dm.close();
        }
    }

    private static void printHelp() {
        System.out.println("usage: java -jar dm.jar <list_of_donloads> <destination_folder>");
    }

}
