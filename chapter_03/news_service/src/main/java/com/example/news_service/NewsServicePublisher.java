package com.example.news_service;

import com.example.news_service.dto.NewsLetter;
import com.mongodb.reactivestreams.client.MongoClient;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.function.Consumer;

public class NewsServicePublisher implements Publisher<NewsLetter> {

    final SmartMulticastProcessor processor;

    public NewsServicePublisher(MongoClient client, String categoryOfInterests) {
        this.processor = new SmartMulticastProcessor();
    }

    @Override
    public void subscribe(Subscriber<? super NewsLetter> s) {
        processor.subscribe(s);
    }
}
