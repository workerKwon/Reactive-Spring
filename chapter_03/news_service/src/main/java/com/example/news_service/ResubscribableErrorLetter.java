package com.example.news_service;

import com.example.news_service.dto.NewsLetter;
import org.reactivestreams.Subscriber;

public interface ResubscribableErrorLetter {
    void resubscribe(Subscriber<? super NewsLetter> subscriber);
}
