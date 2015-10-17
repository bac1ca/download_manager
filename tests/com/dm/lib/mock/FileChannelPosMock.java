package com.dm.lib.mock;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Mock class for FileChannel,
 * overloads methods: write(buf), size(), read(buf, pos), write(buf, pos),
 * toString() and provide additional: data()
 */
public class FileChannelPosMock extends FileChannelMock {

    private long position = 0;

    public FileChannelPosMock(byte[] data) {
        super(data);
    }

    public FileChannelPosMock(long size) {
        super(size);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int writeBytes = write(src, position);
        position += writeBytes;
        return writeBytes;
    }

}