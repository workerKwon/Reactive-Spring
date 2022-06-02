package com.spring.reactive.search_engine;

import org.junit.jupiter.api.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

public class SearchEngineTest {
    @Test
    public void deferSynchronousRequest() throws InterruptedException {
        String query = "query...";
        Observable.fromCallable(() -> doSlowSyncRequest(query))
                .subscribeOn(Schedulers.io())
                .subscribe(this::processResult);

        Thread.sleep(1000);
    }

    private String doSlowSyncRequest(String query) {
        return "result";
    }

    private void processResult(String result) {
        System.out.println(Thread.currentThread().getName() + " : " + result);
    }
}
