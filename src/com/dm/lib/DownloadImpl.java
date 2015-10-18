package com.dm.lib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

public class DownloadImpl implements Download {
    private final URL url;
    private final Path dst;
    private SingeLoad task;

    private static final int CONNECTION_TIMEOUT = Integer.parseInt(
            System.getProperty(DownloadManager.CONNECTION_TIMEOUT_PROP, "5000"));

    /**
     * @param url the source url
     * @param dst destination path
     * @param executor ExecutorService
     * @throws IOException if any IO error occurs
     */
    DownloadImpl(URL url, Path dst, ExecutorService executor)
        throws IOException {

        this.url = url;
        this.dst = dst;

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(CONNECTION_TIMEOUT);
        con.connect();

        final int code = con.getResponseCode();
        System.out.println("info: responseCode - " + code);
        //if (code == HttpURLConnection.HTTP_PARTIAL) { // TODO TBD Multi-connection

        final InputStream src = new BufferedInputStream(con.getInputStream());
        final long contentLen = con.getContentLengthLong();
        task = new SingeLoad(src, dst, contentLen, executor);
    }

    /**
     * @see Download#getSource()
     */
    @Override
    public URL getSource() {
        return url;
    }

    /**
     * @see Download#getDestination()
     */
    @Override
    public Path getDestination() {
        return dst;
    }

    /**
     * @see Download#getProgress()
     */
    @Override
    public double getProgress() {
        return task.getProgress();
    }

    /**
     * @see Download#getContentLen()
     */
    @Override
    public long getContentLen() {
        return task.getContentLen();
    }

    /**
     * @see Download#getStatus()
     */
    @Override
    public Status getStatus() {
        return task.getStatus();
    }

    /**
     * @see Download#cancel()
     */
    @Override
    public void cancel() {
        task.cancel();
    }

    /**
     * @see Download#waitForFinish()
     */
    @Override
    public Status waitForFinish() throws InterruptedException {
        return task.waitForFinish();
    }

    /**
     * @see Download#setStatusListener(StatusListener)
     */
    @Override
    public void setStatusListener(StatusListener listener) {
        task.setStatusListener(listener);
    }

    @Override
    public String toString() {
        return "source: " + getSource() + ", destination: " + dst +
               ", status: " + getStatus();
    }

    /**
     * Internal method, which is called by "process" thread
     * @return false if download task is finished,
     *         true otherwise
     */
    boolean process() {
        return task.process();
    }
}
