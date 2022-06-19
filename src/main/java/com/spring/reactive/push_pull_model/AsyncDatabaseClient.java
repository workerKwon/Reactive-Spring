package com.spring.reactive.push_pull_model;

import org.reactivestreams.Publisher;

public interface AsyncDatabaseClient {
    Publisher<Item> getStreamOfItems();
}
