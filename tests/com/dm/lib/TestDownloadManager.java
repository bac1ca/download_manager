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

import static java.nio.file.StandardOpenOption.*;
import static org.junit.Assert.*;

public class TestDownloadManager {

    @org.junit.Test
    public void testDownloadManager() throws Exception {
        DownloadManager dm = new DownloadManager();
        try {
            dm.start();
            final int N = Runtime.getRuntime().availableProcessors();
            assertEquals(N, dm.getPoolSize());

            dm.setPoolSize(32);
            assertEquals(32, dm.getPoolSize());

            try {
                dm.setPoolSize(-32);
                fail();
            } catch (IllegalArgumentException e) {
                assertEquals(32, dm.getPoolSize());
            }
            assertTrue(dm.activeDownloads().isEmpty());
            assertTrue(dm.finishedDownloads().isEmpty());
        } finally {
            dm.close();
        }
    }

    @org.junit.Test
    public void testDownloadManager2() throws Exception {
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


        DownloadManager dm = new DownloadManager(32);
        assertEquals(32, dm.getPoolSize());

        try {
            server.start();
            dm.start();

            assertTrue(dm.activeDownloads().isEmpty());
            assertTrue(dm.finishedDownloads().isEmpty());

            URL url = new URL("http://localhost:" + PORT + "/test");
            Path out = Paths.get("./test_" + PORT + ".data");

            Download download = dm.download(url, out);
            assertNotNull(download);

            download.waitForFinish();

            assertEquals(url, download.getSource());
            assertEquals(out, download.getDestination());
            assertEquals(1.0, download.getProgress(), 0.001);
            assertEquals(data.length, download.getContentLen());
            assertEquals(State.COMPLETED, download.getStatus().getState());
            assertNull(download.getStatus().getError());

            byte dst[] = readAndDelete(out);
            assertArrayEquals(data, dst);
        } finally {
            dm.close();
            server.stop();
        }
    }

    @org.junit.Test
    public void testDownloadManagerNegative() throws Exception {
        try {
            DownloadManager dm = new DownloadManager(-32);
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        }

        DownloadManager dm = new DownloadManager(32);

        try {
            dm.start();

            assertTrue(dm.activeDownloads().isEmpty());
            assertTrue(dm.finishedDownloads().isEmpty());

            URL url = new URL("http://localhost:8000/no_file.txt");
            Path out = Paths.get("./downloaded_no_file.txt");

            try {
                Download download = dm.download(url, out);
                fail();
            } catch (IOException e) {
                // OK
            }
        } finally {
            dm.close();
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
