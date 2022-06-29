package com.example.pub_sub;


import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Flow;

/**
 * subscriber가 publisher 구독
 * subscription이 publisher에게 요청을 보냄
 * subscriber가 onNext로 받아옴.
 * @param <T>
 */
public class EndSubscriber<T> implements Flow.Subscriber<T> {
    private Flow.Subscription subscription;
    public List<T> consumedElements = new LinkedList<>();

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(T item) {
        System.out.println("Got : " + item);
        consumedElements.add(item);
        subscription.request(1);
    }

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    @Override
    public void onComplete() {
        System.out.println("Done");
    }
}
