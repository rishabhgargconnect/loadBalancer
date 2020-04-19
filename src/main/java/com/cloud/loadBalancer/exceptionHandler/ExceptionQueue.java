package com.cloud.loadBalancer.exceptionHandler;


import com.cloud.loadBalancer.beans.HttpRequestAllParamaters;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ExceptionQueue {

    private static Queue<HttpRequestAllParamaters> exceptionHandleQueue = new ConcurrentLinkedQueue();

    public static void addToExceptionHandleQueue(HttpRequestAllParamaters o) {
        exceptionHandleQueue.add(o);
    }

    public static HttpRequestAllParamaters getFromExceptionHandleQueue() {
        return exceptionHandleQueue.poll();
    }

    public static int exceptionQueueLength() {

        return exceptionHandleQueue.size();
    }
}
