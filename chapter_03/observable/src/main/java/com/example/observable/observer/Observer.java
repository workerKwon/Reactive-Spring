package com.example.observable.observer;

public interface Observer<T> {
    void observe(T event);
}
