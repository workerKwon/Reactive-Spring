package com.spring.reactive.rx_java_temperature;

import org.springframework.stereotype.Component;
import rx.Observable;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class TemperatureSensor {
    private final Random rnd = new Random();

    private final Observable<RxTemperature> dataStream =
            Observable.range(0, Integer.MAX_VALUE)
                    .concatMap(tick -> {
                        System.out.println("tick : " + tick);
                        return Observable
                                .just(tick)
                                .delay(rnd.nextInt(5000), TimeUnit.MILLISECONDS)
                                .map(tickValue -> {
                                    System.out.println("tickValue : " + tickValue);
                                    return this.probe();
                                });
                    })
                    .publish()
                    .refCount();

    private RxTemperature probe() {
        return new RxTemperature(16 + rnd.nextGaussian() * 10);
    }

    public Observable<RxTemperature> temperatureStream() {
        return dataStream;
    }
}
