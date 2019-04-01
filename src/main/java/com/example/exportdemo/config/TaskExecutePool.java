package com.example.exportdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableAsync
public class TaskExecutePool {
    @Bean("exportExecutor")
    public ThreadPoolExecutor getExportExecutor() {
        // 创建队列
        BlockingQueue blockingQueue = new LinkedBlockingDeque<>(120);
        // 核心5 最大10 队列120 超时60s 拒绝策略报异常
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,10,60, TimeUnit.SECONDS,blockingQueue);
        return threadPoolExecutor;
    }
}
