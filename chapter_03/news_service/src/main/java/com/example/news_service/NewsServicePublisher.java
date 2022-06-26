package com.example.news_service;

import com.example.news_service.dto.NewsLetter;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class NewsServicePublisher implements Publisher<NewsLetter> {
    @Override
    public void subscribe(Subscriber<? super NewsLetter> s) {

    }
}
