package com.dm.lib;


import com.dm.lib.mock.FileChannelMock;
import com.dm.lib.mock.FileChannelPosMock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dm.lib.DJobBase.State;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

public class TestDJobMocked {

    @org.junit.Test
    public void testDownloadTask1() throws Exception {
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
            DJobBase djob = new DJobBase(src, dst, -1, executor);
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


}
