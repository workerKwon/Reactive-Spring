package com.example.observable.observer;


import org.junit.jupiter.api.Test;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class RxJavaTest {

    @Test
    @SuppressWarnings("Depricated")
    public void rxJava() {
        Observable<String> observable = Observable.create(
                new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> sub) {
                        sub.onNext("Hello, reactive world!");
                        sub.onCompleted();
                    }
                }
        );
        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {
                System.out.println("Done!");
            }

            @Override
            public void onError(Throwable e) {
                System.err.println(e);
            }

            @Override
            public void onNext(String s) {
                System.out.println(s);
            }
        };

        observable.subscribe(subscriber);
    }


    @Test
    @SuppressWarnings("Depricated")
    public void rxJavaWithLambdas() {
        Observable.create(subscriber -> {
                    subscriber.onNext("Hello, world!");
                    subscriber.onCompleted();
                })
                .subscribe(
                        System.out::println,
                        System.err::println,
                        () -> System.out.println("Done!")
                );
    }

    @Test
    public void creatingRxStreams() {
        Observable.just("1", "2", "3", "4").subscribe(System.out::println);
        Observable.from(new String[]{"A", "B", "C"}).subscribe(System.out::println);
        Observable.from(Collections.<String>emptyList()).subscribe(System.out::println);

        Observable<String> hello = Observable.fromCallable(() -> "Hello ");
        Future<String> future = Executors.newCachedThreadPool().submit(() -> "World");
        Observable<String> world = Observable.from(future);
        Observable.concat(hello, world, Observable.just("!"))
                .forEach(System.out::println);
    }

    @Test
    public void timeBasedSequence() throws InterruptedException {
        Observable.interval(1, TimeUnit.SECONDS)
                .subscribe(e -> System.out.println("Received: " + e));

        Thread.sleep(10000);
    }

    @Test
    public void managingSubscription() throws InterruptedException {
        AtomicReference<Subscription> subscription = new AtomicReference<>();
        subscription.set(Observable.interval(100, TimeUnit.MILLISECONDS)
                .subscribe(e -> {
                    System.out.println("Received: " + e);
                    if (e >= 3) {
                        subscription.get().unsubscribe();
                    }
                })
        );

        Thread.sleep(10000);
        do {
            System.out.println("isUnsubscribed : " + subscription.get().isUnsubscribed());
        } while (!subscription.get().isUnsubscribed());
    }

    @Test
    public void managingSubscription2() throws InterruptedException {
        CountDownLatch externalSignal = new CountDownLatch(3);

        Subscription subscription = Observable
                .interval(100, TimeUnit.MILLISECONDS)
                .subscribe(System.out::println);

        externalSignal.await(850, TimeUnit.MILLISECONDS);
        subscription.unsubscribe();
    }

    @Test
    public void zipOperator() {
        Observable.zip(
                Observable.just("A", "B", "C"),
                Observable.just("1", "2", "3"),
                (x, y) -> x + y
        ).forEach(System.out::println);
    }
}
