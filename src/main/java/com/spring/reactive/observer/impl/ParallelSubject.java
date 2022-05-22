package com.spring.reactive.observer.impl;

import com.spring.reactive.observer.Observer;
import com.spring.reactive.observer.Subject;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelSubject implements Subject<String> {

    private final Set<Observer<String>> observers = new CopyOnWriteArraySet<>();
    @Override
    public void registerObserver(Observer<String> observer) {
        observers.add(observer);
    }

    @Override
    public void unregisterObserver(Observer<String> observer) {
        observers.remove(observer);
    }

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void notifyObservers(String event) {
        observers.forEach(observer ->
                executorService.submit(() -> observer.observe(event)));
    }
}
