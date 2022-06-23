package com.example.observable.push_pull_model;

import org.reactivestreams.Publisher;

public interface AsyncDatabaseClient {
    Publisher<Item> getStreamOfItems();
}
