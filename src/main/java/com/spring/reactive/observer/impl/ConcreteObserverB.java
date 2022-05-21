package com.spring.reactive.observer.impl;

import com.spring.reactive.observer.Observer;

public class ConcreteObserverB implements Observer<String> {
    @Override
    public void observe(String event) {
        System.out.println("Observer B : " + event);
    }
}
