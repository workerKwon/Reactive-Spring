package com.example.news_service;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 정기적으로 실행하고 데이터를 발행하는 클래스
 */
public class ScheduledPublisher<T> implements Publisher<T> {

    final ScheduledExecutorService scheduledExecutorService;
    final int period;
    final TimeUnit unit;
    final Callable<? extends Publisher<T>> publisherCallable;

    public ScheduledPublisher(Callable<? extends Publisher<T>> publisherCallable,
                              int period,
                              TimeUnit unit) {
        this(Executors.newSingleThreadScheduledExecutor(), period, unit, publisherCallable);
    }

    public ScheduledPublisher(ScheduledExecutorService scheduledExecutorService,
                              int period,
                              TimeUnit unit,
                              Callable<? extends Publisher<T>> publisherCallable) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.period = period;
        this.unit = unit;
        this.publisherCallable = publisherCallable;
    }

    @Override
    public void subscribe(Subscriber<? super T> s) {

    }
}
