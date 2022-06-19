package com.spring.reactive.push_pull_model;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

public class DelayedFakeAsyncDatabaseClient implements AsyncDatabaseClient {

    @Override
    public Publisher<Item> getStreamOfItems() {
        return Flowable.range(1, Integer.MAX_VALUE)
                .map(integer -> new Item("" + integer))
                .delay(50, TimeUnit.MILLISECONDS)
                .hide()
                .subscribeOn(Schedulers.io())
                .delaySubscription(100, TimeUnit.MILLISECONDS);
    }
}
