package com.sjh;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class RunAddressThreadFactory implements ThreadFactory{

    private AtomicInteger threadIdx = new AtomicInteger(0);

    private String threadNamePrefix;

    public RunAddressThreadFactory(String Prefix) {
        threadNamePrefix = Prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(threadNamePrefix + "-job-" + threadIdx.getAndIncrement());
        return thread;
    }

}
