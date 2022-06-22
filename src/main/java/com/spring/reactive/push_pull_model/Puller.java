package com.spring.reactive.push_pull_model;

import org.reactivestreams.Publisher;

public class Puller {
    final AsyncDatabaseClient dbClient = new DelayedFakeAsyncDatabaseClient();

    public Publisher<Item> list(int count) {
        Publisher<Item> source = dbClient.getStreamOfItems();

        /**
         * publisher를 상속하고 subscriber와 subscription을 구현한 inner class로 바로 subscribe를 한다.
         */
        TakeFilterOperator<Item> takeFilter = new TakeFilterOperator<>(source, count, this::isValid);
        return takeFilter;
    }

    boolean isValid(Item item) {
        return Integer.parseInt(item.getId()) % 2 == 0;
    }
}
