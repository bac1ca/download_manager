package com.dm.lib.mock;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Mock class for FileChannel,
 * overloads methods: size(), read(buf, pos), write(buf, pos), toString()
 * and provide additional: data()
 */
public class FileChannelMock extends FileChannelMockBase {

    public FileChannelMock(byte[] data) {
        super(data);
    }

    public FileChannelMock(long size) {
        super(size);
    }

    @Override
    public int read(ByteBuffer dst, long position) throws IOException {
        final int idxInit = (int) position;
        int idx = idxInit;

        while (dst.position() < dst.limit()) {
            if (idx == data.length) {
                break;
            }
            dst.put(data[idx++]);
        }
        int readBytes = idx - idxInit;
        return readBytes;
    }

    @Override
    public int write(ByteBuffer src, long position) throws IOException {
        final int idxInit = (int) position;
        int idx = idxInit;

        while (src.hasRemaining()) {
            data[idx++] = src.get();
        }
        int writeBytes = idx - idxInit;
        return writeBytes;
    }

}