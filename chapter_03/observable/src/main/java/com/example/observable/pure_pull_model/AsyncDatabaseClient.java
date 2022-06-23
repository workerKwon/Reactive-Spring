package com.example.observable.pure_pull_model;


import java.util.concurrent.CompletionStage;

public interface AsyncDatabaseClient {
    CompletionStage<Item> getNextAfterId(String id);
}
