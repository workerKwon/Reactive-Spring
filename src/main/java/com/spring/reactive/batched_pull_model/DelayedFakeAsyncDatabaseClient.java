package com.spring.reactive.batched_pull_model;

import io.reactivex.Flowable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class DelayedFakeAsyncDatabaseClient implements AsyncDatabaseClient{
    @Override
    public CompletionStage<List<Item>> getNextBatchAfterId(String id, int count) {
        CompletableFuture<List<Item>> future = new CompletableFuture<>();

        /**
         * Flowable - reactive stream data를 구현하고 소비할 수 있는 연산자 및 메소드를 제공하는 클래스
         */
        Flowable.range(Integer.parseInt(id) + 1, count)
                .map(i -> new Item("" + i))
                .collectInto(new ArrayList<Item>(), ArrayList::add)
                .delay(1000, TimeUnit.MILLISECONDS)
                .subscribe(future::complete);
        return future;
    }
}
