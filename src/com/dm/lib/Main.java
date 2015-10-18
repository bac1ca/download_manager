package com.dm.lib;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.dm.lib.Download.State;
import com.dm.lib.Download.Status;

/**
 * Simple utility for downloading files via http
 */
public class Main {

    public static void main(String[] args) {
        // for tests only
//        args = new String [] {
//                "http://localhost:8000/file1.pdf",
//                "http://localhost:8001/file2.pdf",
//                "./downloads/"
//        };

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
                            ", " + e.getCause());
                }
            }

        } finally {
            try {
                dm.close();
            } catch (InterruptedException ignored) {}
        }
    }

    private static void printHelp() {
        System.out.println("usage: java -jar dm.jar <list_of_donloads> <destination_folder>");
    }}
