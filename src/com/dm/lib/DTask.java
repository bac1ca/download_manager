package com.dm.lib;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;

public class DTask implements Callable<Integer> {

    private final InputStream src;
    private final FileChannel dst;

    private final ThreadLocal<byte[]> dataTL;
//    private final static int DEFAULT_BUFFER_SIZE = 1024 * 4; // 4Kb TODO
    private final static int DEFAULT_BUFFER_SIZE = 4 * 1024; // 4Kb TODO

    public DTask(InputStream src, FileChannel dst) {
        this(src, dst, DEFAULT_BUFFER_SIZE);
    }

    public DTask(InputStream src, FileChannel dst, int buffSize) {
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

            // TODO Progress Granularity
//            while(((numRead = src.read(data, 0, data.length)) != -1)) {
//                final ByteBuffer buf = ByteBuffer.wrap(data, 0, numRead);
//                dst.write(buf);
//            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
