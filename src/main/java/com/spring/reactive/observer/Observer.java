package com.spring.reactive.observer;

public interface Observer<T> {
    void observe(T event);
}
