package com.spring.reactive.temperature;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class TemperatureSensor {
    private final ApplicationEventPublisher publisher; // 이벤트를 시스템에 발행
    private final Random random = new Random();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public TemperatureSensor(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostConstruct // 빈이 생성될 때 스프링 프레임워크에 의해 호출돼 probe 메소드 실행을 예약
    public void startProcessing() {
        this.executor.schedule(this::probe, 1, TimeUnit.SECONDS);
    }

    private void probe() {
        double temperature = 16 + random.nextGaussian() * 10;
        publisher.publishEvent(new Temperature(temperature)); // 이벤트 발행. @EventListener 가 이벤트를 수신.
        executor.schedule(this::probe, random.nextInt(5000), TimeUnit.MILLISECONDS); // 다음 probe 메소드 실행 예약
    }
}
