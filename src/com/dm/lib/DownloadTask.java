package com.dm.lib;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;

/**
 * Download task for running in the ExecutorService
 */
class DownloadTask implements Callable<Integer> {

    private final InputStream src;
    private final FileChannel dst;

    private final ThreadLocal<byte[]> dataTL;
    private static final int DEFAULT_BUFFER_SIZE = Integer.parseInt(
        System.getProperty(DownloadManager.BUFFER_SIZE_PROP, "1048576"));

    DownloadTask(InputStream src, FileChannel dst) {
        this(src, dst, DEFAULT_BUFFER_SIZE);
    }

    DownloadTask(InputStream src, FileChannel dst, int buffSize) {
        this.src = src;
        this.dst = dst;

        dataTL = new ThreadLocal<byte[]>() {
            @Override
            protected byte[] initialValue() {
                return new byte[buffSize];
            }
        };
    }

    public Integer call() {
        try {
            byte[] data = dataTL.get();
            int numRead = src.read(data, 0, data.length);
            if (numRead != -1) {
                final ByteBuffer buf = ByteBuffer.wrap(data, 0, numRead);
                dst.write(buf);
            }
            return numRead;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
