package com.example.observable.observer;

public interface RxObserver<T> {
    void onNext(T next);
    void onComplete();
    void onError(Exception e);
}
