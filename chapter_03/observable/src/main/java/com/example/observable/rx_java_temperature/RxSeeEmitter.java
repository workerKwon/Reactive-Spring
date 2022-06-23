package com.example.observable.rx_java_temperature;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import rx.Subscriber;

import java.io.IOException;

public class RxSeeEmitter extends SseEmitter {
    static final long SSE_SESSION_TIMEOUT = 30 * 60 * 1000L;
    private final Subscriber<RxTemperature> subscriber;

    RxSeeEmitter() {
        super(SSE_SESSION_TIMEOUT);

        this.subscriber = new Subscriber<RxTemperature>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(RxTemperature rxTemperature) {
                try {
                    RxSeeEmitter.this.send(rxTemperature);
                } catch (IOException e) {
                    unsubscribe();
                }
            }
        };

        onCompletion(subscriber::unsubscribe);
        onTimeout(subscriber::unsubscribe);
    }

    Subscriber<RxTemperature> getSubscriber() {
        return subscriber;
    }
}
