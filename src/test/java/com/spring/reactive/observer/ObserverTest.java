package com.spring.reactive.observer;


import com.spring.reactive.observer.impl.ConcreteObserverA;
import com.spring.reactive.observer.impl.ConcreteObserverB;
import com.spring.reactive.observer.impl.ConcreteSubject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
}
