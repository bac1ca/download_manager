
package com.dm.lib;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DManager {

    private final List<DJob> tasks = new ArrayList<>();
    private final ExecutorService executor;

    public DManager() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public DManager(int threadPoolSize) {
        executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    public int getThreadPoolSize() {
        return ((ThreadPoolExecutor) executor).getCorePoolSize();
    }

    public void setThreadPoolSize(int amount) {
        ((ThreadPoolExecutor) executor).setCorePoolSize(amount);
        ((ThreadPoolExecutor) executor).setMaximumPoolSize(amount);
    }

    public List<DJob> downloadList() {
        return new ArrayList<>(tasks);
    }

    public DJob download(URL src, Path dst) throws IOException {
        HttpURLConnection con = (HttpURLConnection)src.openConnection();
        DJob dt = new DJob(con, dst, executor);

        tasks.add(dt); // TODO concurrent list
        return dt;
    }

    public void close() {
        executor.shutdown();
    }

    public static void main(String[] args) throws Exception {
        //new SimpleHTTPServer(8080, "/test", new HttpRequestHandler()).start();

        String addr = "http://localhost:8000/RangeHTTPServer.py";
        URL myURL = new URL(addr);

        HttpURLConnection conn = (HttpURLConnection)myURL.openConnection();


        String byteRange = 0 + "-" + 128;
        conn.setRequestProperty("Range", "bytes=" + byteRange);
        System.out.println("bytes=" + byteRange);

        // connect to server
        conn.connect();

        String field = conn.getHeaderField("Accept-Ranges");
        System.out.println("f: " + field);

        int code = conn.getResponseCode();
        System.out.println("code = " + code);


        // get the input stream
        InputStream in = new BufferedInputStream(conn.getInputStream());

        final int BUFFER_SIZE = 32;
        byte data[] = new byte[BUFFER_SIZE];
        int numRead;
        while(((numRead = in.read(data, 0, BUFFER_SIZE)) != -1))
        {
            System.out.println("numRead = " + numRead);
        }

    }

}
