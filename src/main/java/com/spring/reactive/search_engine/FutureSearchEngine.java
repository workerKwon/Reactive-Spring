package com.spring.reactive.search_engine;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FutureSearchEngine {
    CompletableFuture<List<URL>> search(String query, int limit);
}
