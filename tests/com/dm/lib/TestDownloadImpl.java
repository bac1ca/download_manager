package com.dm.lib;


import com.dm.lib.Download.State;
import com.dm.lib.server.TinyHTTPServer;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.Assert.*;

public class TestDownloadImpl {

    @org.junit.Test
    public void testDownloadNegative() throws Exception {
        URL url = new URL("http://localhost:8000/no_file.txt");
        Path out = Paths.get("./downloaded_no_file.txt");
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            DownloadImpl download = new DownloadImpl(url, out, executor);
            fail("IOE should be thrown");
        } catch (IOException e) {
            // OK
        } finally {
            executor.shutdown();
        }
    }



    @org.junit.Test
    public void testDownload() throws Exception {
        final int PORT = 9001;

        byte[] data = {
                1, 2, 3, 4, 5, 6, 7, 8,
                1, 2, 3, 4, 5, 6, 7, 8,
                1, 2, 3, 4, 5, 6, 7, 8,
                1, 2, 3, 4, 5, 6, 7, 8,
                1, 2, 3, 4, 5, 6, 7, 8,
                1, 2, 3, 4, 5, 6, 7, 8,
                1, 2, 3, 4, 5, 6, 7, 8,
                1, 2, 3, 4, 5, 6, 7, 8,
        };

        TinyHTTPServer server = new TinyHTTPServer(PORT, data);
        server.start();

        URL url = new URL("http://localhost:" + PORT + "/test");
        Path out = Paths.get("./test_" + PORT + ".data");
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            DownloadImpl download = new DownloadImpl(url, out, executor);
            while (download.process()) {}

            byte dst[] = readAndDelete(out);
            assertArrayEquals(data, dst);
        } finally {
            executor.shutdown();
            server.stop();
        }
    }

    @org.junit.Test
    public void testDownload2() throws Exception {
        final int PORT = 9002;

        byte[] data = {
                1, 2, 3, 4, 5, 6, 7, 8,
                1, 2, 3, 4, 5, 6, 7, 8,
                1, 2, 3, 4, 5, 6, 7, 8,
                1, 2, 3, 4, 5, 6, 7, 8,
                1, 2, 3, 4, 5, 6, 7, 8,
                1, 2, 3, 4, 5, 6, 7, 8,
                1, 2, 3, 4, 5, 6, 7, 8,
                1, 2, 3, 4, 5, 6, 7, 8,
        };

        TinyHTTPServer server = new TinyHTTPServer(PORT, data);
        server.start();

        URL url = new URL("http://localhost:" + PORT + "/test");
        Path out = Paths.get("./test_" + PORT + ".data");
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            DownloadImpl download = new DownloadImpl(url, out, executor);

            // check all getters
            assertEquals(url, download.getSource());
            assertEquals(out, download.getDestination());
            assertEquals(0.0, download.getProgress(), 0.001);
            assertEquals(data.length, download.getContentLen());
            assertEquals(State.INPROGRESS, download.getStatus().getState());
            assertNull(download.getStatus().getError());

            assertTrue(download.process());

            while (download.process()) {}
            assertEquals(State.COMPLETED, download.getStatus().getState());

            // there is no data to process
            assertFalse(download.process());

            byte dst[] = readAndDelete(out);
            assertArrayEquals(data, dst);
        } finally {
            executor.shutdown();
            server.stop();
        }
    }

    static byte[] readAndDelete(Path file) throws IOException {
        try (FileChannel channel = (FileChannel.open(file, CREATE, READ, WRITE))) {
            long size = channel.size();
            byte[] fileData = new byte[(int) size];
            channel.read(ByteBuffer.wrap(fileData));
            System.out.println(new String(fileData));
            return fileData;
        } finally {
            Files.delete(file);
        }
    }
}
