package com.example.observable.search_engine;

import rx.Observable;

import java.net.URL;

public interface RxSearchEngine {
    Observable<URL> search(String query);
}
