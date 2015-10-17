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

    private final HttpURLConnection con;
    private final Path dst;

    private final DJob task;
//    private List<DJob> tasks = new ArrayList<>();

    private static final int CONNECTION_TIMEOUT = 5_000;

    Download(URL url, Path dst, ExecutorService executor) throws IOException {
        con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(CONNECTION_TIMEOUT);
        con.connect();

        String field = con.getHeaderField("Accept-Ranges");
        System.out.println("f: " + field);

        int code = con.getResponseCode();
        System.out.println("code = " + code);


        final InputStream src = new BufferedInputStream(con.getInputStream());
        final long contentLen = con.getContentLengthLong();
        this.dst = dst;
        task = new DJob(src, dst, contentLen, executor);
    }

    public URL getSource() {
        return con.getURL();
    }

    public Path getDestination() {
        return dst;
    }

    public double getProgress() {
        return task.getProgress();
    }

    public long getContentLen() {
        return -1;  // TODO
    }

    public void cancel() {
        task.cancel();
    }

    public Status waitForFinish() {
        return task.waitForFinish();
    }

    public void setStatusListener(StatusListener listener) {
        task.setStatusListener(listener);
    }

    boolean process() {
        return task.process();
    }

}
