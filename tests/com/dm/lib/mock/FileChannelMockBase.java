package com.dm.lib.mock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

/**
 * Mock class for FileChannel,
 * overloads methods: size(), read(buf, pos), write(buf, pos), toString()
 * and provide additional: data()
 */
public class FileChannelMockBase extends FileChannel {

    protected final byte[] data;

    public FileChannelMockBase(byte[] data) {
        this.data = data;
    }

    public FileChannelMockBase(long size) {
        data = new byte[(int)size];
    }

    @Override
    public long size() throws IOException {
        return data.length;
    }

    @Override
    public String toString() {
        return Arrays.toString(data);
    }

    /**
     * Returns contents of the FileChannel as integer array
     * @return contents of the FileChannel as integer array
     */
    public byte[] data() {
        return data;
    }

    // --------------------------------------------
    // NOT SUPPORTED FOR TESTS
    // --------------------------------------------

    @Override
    public int read(ByteBuffer dst, long position) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int write(ByteBuffer src, long position) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long position() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileChannel position(long newPosition) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileChannel truncate(long size) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void force(boolean metaData) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileLock lock(long position, long size, boolean shared) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void implCloseChannel() throws IOException {
        System.out.println("FileChannelMockBase is closed");
    }
}
