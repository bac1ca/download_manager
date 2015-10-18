package com.dm.lib;


import com.dm.lib.mock.FileChannelMock;
import com.dm.lib.mock.FileChannelPosMock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

public class TestDTaskMocked {

    @org.junit.Test
    public void testDownloadTask1() throws Exception {
        byte[] data = {0, 1, 2, 3, 4, 5, 6, 7};
        InputStream src = new ByteArrayInputStream(data);
        FileChannelMock dst = new FileChannelPosMock(data.length);

        final DownloadTask task = new DownloadTask(src, dst);
        int readBytes = task.call();
        assertEquals(data.length, readBytes);
        assertArrayEquals(data, dst.data());
    }

    @org.junit.Test
    public void testDownloadTask2() throws Exception {
        byte[] data = {0, 1, 2, 3, 4, 5, 6, 7};
        InputStream src = new ByteArrayInputStream(data);
        FileChannelMock dst = new FileChannelPosMock(data.length);

        final int bufSize = 6;
        final DownloadTask task = new DownloadTask(src, dst, bufSize);
        int readBytes = task.call();
        assertEquals(bufSize, readBytes);

        for (int i = 0; i < bufSize; i++) {
            assertEquals(data[i], dst.data()[i]);
        }
        System.out.println(dst);
    }

}
