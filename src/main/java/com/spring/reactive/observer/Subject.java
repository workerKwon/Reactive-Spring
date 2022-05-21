package com.spring.reactive.observer;


public interface Subject<T> {
    void registerObserver(Observer<T> observer);
    void unregisterObserver(Observer observer);
    void notifyObservers(T event);
}
