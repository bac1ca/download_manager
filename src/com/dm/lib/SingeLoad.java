package com.dm.lib;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

import com.dm.lib.Download.State;

/**
 * This class extends {@see SingleLoadBase}, applies destination path
 * parameter, overrides {@see finish} method: destination file will
 * be removed if task status is FAILED
 */
class SingeLoad extends SingleLoadBase {

    private final Path dst;

    SingeLoad(InputStream src, Path dst, long contentLen, ExecutorService executor)
            throws IOException {
        super(src, FileChannel.open(dst, CREATE, WRITE), contentLen, executor);
        this.dst = dst;
    }

    @Override
    protected void finish(State state, Exception error) {
        super.finish(state, error);
        if (getStatus().getError() != null) {
            try {
                Files.delete(dst);
            } catch (IOException ignore){}
        }
    }
}
