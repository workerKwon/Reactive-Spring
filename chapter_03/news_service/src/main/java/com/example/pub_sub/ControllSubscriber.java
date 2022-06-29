package com.example.pub_sub;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

public class ControllSubscriber<T> implements Flow.Subscriber<T> {

    private AtomicInteger howMuchMessagesConsume;
    private Flow.Subscription subscription;
    public List<T> consumedElements = new LinkedList<>();

    public ControllSubscriber(Integer howMuchMessagesConsume) {
        /**
         * max data 개수 설정
         */
        this.howMuchMessagesConsume = new AtomicInteger(howMuchMessagesConsume);
    }

    @Override
    public void onSubscribe(Flow.Subscription s) {
        this.subscription = s;
        subscription.request(1);
    }

    @Override
    public void onNext(T t) {
        howMuchMessagesConsume.decrementAndGet();
        System.out.println(this + " Got : " + t);
        consumedElements.add(t);
        /**
         * 데이터 개수가 1개 이상이면 데이터 요청
         */
        if(howMuchMessagesConsume.get() > 0) {
            subscription.request(1);
        }
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
