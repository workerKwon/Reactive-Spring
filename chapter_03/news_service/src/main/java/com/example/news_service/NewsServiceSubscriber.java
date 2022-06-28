package com.example.news_service;

import com.example.news_service.dto.NewsLetter;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class NewsServiceSubscriber implements Subscriber<NewsLetter> {

    final Queue<NewsLetter> mailbox = new ConcurrentLinkedQueue<>();
    final AtomicInteger remaining = new AtomicInteger();
    final int take;
    Subscription subscription;

    public NewsServiceSubscriber(int take) {
        this.take = take;
        remaining.set(take);
    }

    public void onSubscribe(Subscription s) {
        if (subscription == null) {
            subscription = s;
            subscription.request(take);
        } else {
            s.cancel();
        }
    }

    public void onNext(NewsLetter newsLetter) {
        Objects.requireNonNull(newsLetter);

        mailbox.offer(newsLetter);
    }

    public void onError(Throwable t) {
        Objects.requireNonNull(t);

        if (t instanceof ResubscribableErrorLetter) {
            subscription = null;
            ((ResubscribableErrorLetter) t).resubscribe(this);
        }
    }

    public void onComplete() {
        subscription = null;
    }
}
