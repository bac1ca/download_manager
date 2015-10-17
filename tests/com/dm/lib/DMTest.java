package com.dm.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.Assert.*;

public class DMTest {


    // http://github.com/bac1ca/sort-big-arrays/blob/master/LICENSE


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