package com.dm.lib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

import com.dm.lib.DJobBase.StatusListener;
import com.dm.lib.DJobBase.Status;

public class Download {
    private static final int CONNECTION_TIMEOUT = 5_000;

    private final URL url;
    private final Path dst;
    private final ExecutorService executor;

    private DJob task;
    private int code;

    Download(URL url, Path dst, ExecutorService executor) {
        this.url = url;
        this.dst = dst;
        this.executor = executor;
    }

    void start() throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(CONNECTION_TIMEOUT);
        con.connect();

        code = con.getResponseCode();

//        if (code == HttpURLConnection.HTTP_PARTIAL) {
//            // TODO multiple connections for one file is available
//        }

        final InputStream src = new BufferedInputStream(con.getInputStream());
        final long contentLen = con.getContentLengthLong();
        task = new DJob(src, dst, contentLen, executor);
    }


    public URL getSource() {
        return url;
    }

    public Path getDestination() {
        return dst;
    }

    public double getProgress() {
        return task.getProgress();
    }

    public long getContentLen() {
        return task.getContentLen();
    }

    public int getHttpResponseCode() {
        return code;
    }

    public Status getStatus() {
        return task.getStatus();
    }

    public void cancel() {
        task.cancel();
    }

    public Status waitForFinish() throws InterruptedException {
        return task.waitForFinish();
    }

    public void setStatusListener(StatusListener listener) {
        task.setStatusListener(listener);
    }

    boolean process() {
        return task.process();
    }

    @Override
    public String toString() {
        return "source: " + getSource() + ", destination: " + dst +
               ", status: " + getStatus();
    }
}
