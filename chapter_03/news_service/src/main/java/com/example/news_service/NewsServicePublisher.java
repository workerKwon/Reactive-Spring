package com.example.news_service;

import com.example.news_service.dto.News;
import com.example.news_service.dto.NewsLetter;
import com.mongodb.reactivestreams.client.MongoClient;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.concurrent.TimeUnit;

/**
 * NewsServicePublisher를 구독하면 SmartMulticastProcessor를 구독하게 됨.
 */
public class NewsServicePublisher implements Publisher<NewsLetter> {

    final SmartMulticastProcessor processor;

    /**
     * SmartMulticastProcessor는 DB에서 데이터를 가져오는 ScheduledPublisher를 구독하고 데이터를 가져간다.
     */
    public NewsServicePublisher(MongoClient client, String categoryOfInterests) {
        ScheduledPublisher<NewsLetter> scheduledPublisher = new ScheduledPublisher<>(
                () -> new NewsPreparationOperator(
                        new DBPublisher(
                                client.getDatabase("news").getCollection("news", News.class),
                                categoryOfInterests
                        ),
                        "Some Digest"
                ),
                1, TimeUnit.DAYS
        );
        SmartMulticastProcessor smartMulticastProcessor = new SmartMulticastProcessor();
        scheduledPublisher.subscribe(smartMulticastProcessor);
        this.processor = smartMulticastProcessor;
    }

    @Override
    public void subscribe(Subscriber<? super NewsLetter> s) {
        processor.subscribe(s);
    }
}
