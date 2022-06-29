package com.example.news_service;

import com.example.news_service.dto.News;
import com.example.news_service.dto.NewsLetter;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NewsLetter를 준비하고 발행하는 역할
 */
public class NewsPreparationOperator implements Publisher<NewsLetter> {

    final Publisher<? extends News> upstream;
    final String title;

    public NewsPreparationOperator(Publisher<? extends News> upstream, String title) {
        this.upstream = upstream;
        this.title = title;
    }

    @Override
    public void subscribe(Subscriber<? super NewsLetter> s) {
        upstream.subscribe(new NewsPreparationInner(s, title));
    }

    final static class NewsPreparationInner implements Subscription, Subscriber<News> {

        final Subscriber<? super NewsLetter> subscriber;
        final String title;
        final Queue<News> newsQueue;
        final AtomicBoolean terminated = new AtomicBoolean();

        public NewsPreparationInner(Subscriber<? super NewsLetter> subscriber, String title) {
            this.subscriber = subscriber;
            this.title = title;
            this.newsQueue = new LinkedList<>();
        }

        @Override
        public void onSubscribe(Subscription s) {

        }

        @Override
        public void onNext(News news) {

        }

        @Override
        public void onError(Throwable t) {

        }

        @Override
        public void onComplete() {

        }

        @Override
        public void request(long n) {

        }

        @Override
        public void cancel() {

        }
    }
}
