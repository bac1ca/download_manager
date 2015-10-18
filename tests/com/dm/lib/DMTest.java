package com.dm.lib;

import java.net.URL;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class DMTest {


    // http://github.com/bac1ca/sort-big-arrays/blob/master/LICENSE


    @org.junit.Test
    public void test() throws Exception {
        URL url = new URL("http://github.com/bac1ca/sort-big-arrays/blob/master/LICENSE");

        System.out.println(url.getProtocol().toLowerCase());

        String name = url.getFile();
        System.out.println("name = " + name);

        int idx = name.lastIndexOf('/');
        name = name.substring(idx + 1);
        System.out.println("name = " + name);


    }

    @org.junit.Test
    public void test2() throws Exception {
        int N = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            try {
                System.out.println("aaa");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        executor.submit(() -> {
            try {
                System.out.println("aaa");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ((ThreadPoolExecutor) executor).setCorePoolSize(1);
        ((ThreadPoolExecutor) executor).setMaximumPoolSize(1);

        executor.shutdown();
    }

}