package com.dm.lib;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.dm.lib.Download.State;
import com.dm.lib.Download.Status;
import com.dm.lib.Download.StatusListener;

class SingleLoadBase {
    private final InputStream src;
    private final FileChannel dst;
    private final long contentLen;
    private final ExecutorService executor;

    private volatile long progress = 0;
    private volatile boolean isTerminated = false;
    private volatile Status status = new Status(State.INPROGRESS, null);

    SingleLoadBase(InputStream src, FileChannel dst, long contentLen,
                   ExecutorService executor) {
        this.src = src;
        this.dst = dst;
        this.executor = executor;
        this.contentLen = contentLen;
        if (contentLen == -1) {
            System.out.println("warn: content-contentLen = " + contentLen);
        }
    }

    private Future<Integer> future;

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
        future = executor.submit(new DownloadTask(src, dst));
        return true;
    }


    public Status getStatus() {
        return status;
    }

    public long getContentLen() {
        return contentLen;
    }

    /**
     * @see Download#getProgress()
     */
    public double getProgress() {
        return (double) progress / contentLen;
    }

    public void cancel() {
        isTerminated = true;
        finish(State.CANCELED, null);
    }

    private Object lock = new Object();
    public Status waitForFinish() throws InterruptedException {
        synchronized (lock) {
            while(status.getState() == State.INPROGRESS) {
                lock.wait();
            }
        }
        return status;
    }


    private volatile StatusListener listener;

    public void setStatusListener(StatusListener listener) {
        this.listener = listener;
    }

    protected void finish(State state, Exception error) {
        status = new Status(state, error);
        if (listener != null) listener.notify(status, getProgress());

        try {
            src.close();
        } catch (IOException ignore) {}
        try {
            dst.close();
        } catch (IOException ignore) {}
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
