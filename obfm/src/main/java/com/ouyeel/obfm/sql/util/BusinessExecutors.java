package com.ouyeel.obfm.sql.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BusinessExecutors {

    private static final ThreadFactory citaSqlFactory = new ThreadFactoryBuilder().setNameFormat("cita-sql-pool-%d").build();

    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            4,
            100,
            60,
            TimeUnit.SECONDS, new ArrayBlockingQueue<>(200),
            citaSqlFactory,
            new ThreadPoolExecutor.AbortPolicy());

    public static ThreadPoolExecutor getThreadPool(){
        return threadPoolExecutor;
    }


}
