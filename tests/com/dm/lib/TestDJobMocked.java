package com.dm.lib;


import com.dm.lib.mock.FileChannelMock;
import com.dm.lib.mock.FileChannelPosMock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dm.lib.Download.State;

import static org.junit.Assert.*;

public class TestDJobMocked {

    @org.junit.Test
    public void testDownloadTask() throws Exception {
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

        final InputStream src = new ByteArrayInputStream(data);
        final FileChannelMock dst = new FileChannelPosMock(data.length);

        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            SingleLoadBase djob = new SingleLoadBase(src, dst, -1, executor);
            while (djob.process()) {}

            State state = djob.getStatus().getState();
            if (state == State.COMPLETED) {
                assertArrayEquals(data, dst.data());
            } else if (state == State.FAILED) {
                assertNotNull(djob.getStatus().getError());
            }
        } finally {
            executor.shutdown();
        }
    }

    @org.junit.Test
    public void testDownloadTaskStatus() throws Exception {
        final byte[] data = { 1, 2, 3, 4, 5, 6, 7, 8};
        final InputStream src = new ByteArrayInputStream(data);
        final FileChannelMock dst = new FileChannelPosMock(data.length);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            SingleLoadBase job = new SingleLoadBase(src, dst, -1, executor);
            assertEquals(State.INPROGRESS, job.getStatus().getState());

            assertTrue(job.process());
            assertEquals(State.INPROGRESS, job.getStatus().getState());

            job.cancel();
            assertFalse(job.process());
            assertEquals(State.CANCELED, job.getStatus().getState());
            assertNull(job.getStatus().getError());
        } finally {
            executor.shutdown();
        }
    }


}
