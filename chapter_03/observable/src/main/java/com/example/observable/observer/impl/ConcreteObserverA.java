package com.example.observable.observer.impl;

import com.example.observable.observer.Observer;

public class ConcreteObserverA implements Observer<String> {
    @Override
    public void observe(String event) {
        System.out.println("Observer A : " + event);
    }
}
