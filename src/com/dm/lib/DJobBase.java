package com.dm.lib;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class DJobBase {
    private final InputStream src;
    private final FileChannel dst;
    private final long contentLen;
    private final ExecutorService executor;

    private long progress = 0;
    private volatile boolean isTerminated = false;
    private volatile Status status = new Status(State.INPROGRESS, null);

    DJobBase(InputStream src, FileChannel dst, long contentLen,
             ExecutorService executor) {
        this.src = src;
        this.dst = dst;
        this.executor = executor;
        this.contentLen = contentLen;
        if (contentLen == -1) {
            System.out.println("warn: content-contentLen = " + contentLen);
        }
    }

    boolean process() {
        if (isTerminated) {
            return false;
        }

        if (future != null) {
            try {
                int readBytes = future.get();
                if (readBytes == -1) {
                    finish(State.COMPLETED, null);
                    return false;
                }
                progress += readBytes;
                if (listener != null) listener.notify(status, getProgress());

            } catch (InterruptedException | ExecutionException e) {
                finish(State.FAILED, e);
                return false;
            }
        }
        future = executor.submit(new DTask(src, dst));
        return true;
    }
    private Future<Integer> future;


    public enum State {
        INPROGRESS,
        COMPLETED,
        CANCELED,
        FAILED,
    }

    public class Status {

        private final State state;
        private final Exception error;

        private Status(State state, Exception error) {
            this.state = state;
            this.error = error;
        }

        public State getState() {
            return state;
        }

        public Exception getError() {
            return error;
        }
    }

    public Status getStatus() {
        return status;
    }

    /**
     * Returns Download Job progress [0.0, 1.0]; TODO description (< 0)
     * @return
     */
    public double getProgress() {
        return (double) progress / contentLen;
    }

    public void cancel() {
        isTerminated = true;
        finish(State.CANCELED, null);
    }

    public Status waitForFinish() {
        // TODO
        return null;
    }

    public interface StatusListener {
        void notify(Status status, double progress);
    }

    private volatile StatusListener listener;

    public void setStatusListener(StatusListener listener) {
        this.listener = listener;
    }

    protected void finish(State state, Exception error) {
        status = new Status(state, error);
        try {
            src.close();
        } catch (IOException ignore) {}
        try {
            dst.close();
        } catch (IOException ignore) {}
    }
}
