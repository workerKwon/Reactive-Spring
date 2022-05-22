package com.spring.reactive.observer;


import com.spring.reactive.observer.impl.ConcreteObserverA;
import com.spring.reactive.observer.impl.ConcreteObserverB;
import com.spring.reactive.observer.impl.ConcreteSubject;
import com.spring.reactive.observer.impl.ParallelSubject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class ObserverTest {

    @Test
    public void observerHandleEventsFromSubject() {
        // given
        Subject<String> subject = new ConcreteSubject();
        Observer<String> observerA = Mockito.spy(new ConcreteObserverA());
        Observer<String> observerB = Mockito.spy(new ConcreteObserverB());

        // when
        subject.notifyObservers("No listeners");

        subject.registerObserver(observerA);
        subject.notifyObservers("Message for A");

        subject.registerObserver(observerB);
        subject.notifyObservers("Message for A & B");

        subject.unregisterObserver(observerA);
        subject.notifyObservers("Message for B");

        subject.unregisterObserver(observerB);
        subject.notifyObservers("No listeners");

        // then
        Mockito.verify(observerA, Mockito.times(1)).observe("Message for A");
        Mockito.verify(observerA, Mockito.times(1)).observe("Message for A & B");
        Mockito.verifyNoMoreInteractions(observerA);

        Mockito.verify(observerB, Mockito.times(1)).observe("Message for A & B");
        Mockito.verify(observerB, Mockito.times(1)).observe("Message for B");
        Mockito.verifyNoMoreInteractions(observerB);
    }

    @Test
    public void subjectLeveragesLambdas() {
        Subject<String> subject = new ConcreteSubject();

        subject.registerObserver(e -> System.out.println("A: " + e));
        subject.registerObserver(e -> System.out.println("B: " + e));
        subject.notifyObservers("This message will receive A & B");
    }

    @Test
    public void parallelSubject() {
        Subject<String> subject = new ParallelSubject();

        Supplier<String> thread = () -> Thread.currentThread().getName();
        Function<String, Observer<String>> generateObserver = (String name) ->
                (e -> System.out.println(thread.get() + " | " + name + ": " + e));

        subject.registerObserver(generateObserver.apply("A"));
        subject.registerObserver(generateObserver.apply("B"));

        IntStream.range(0, 10).forEach(i -> subject.notifyObservers("Temperature " + i));
    }
}
