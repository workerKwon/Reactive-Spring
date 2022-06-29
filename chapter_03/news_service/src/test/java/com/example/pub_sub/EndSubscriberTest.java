package com.example.pub_sub;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

public class EndSubscriberTest {

    @Test
    public void whenSubscribeToIt_thenShouldConsumeAll() {
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
        EndSubscriber<String> subscriber1 = new EndSubscriber<>();
        EndSubscriber<String> subscriber2 = new EndSubscriber<>();
        publisher.subscribe(subscriber1);
        publisher.subscribe(subscriber2);
        List<String> items = List.of("1", "x", "2", "x", "3", "x");

        assertThat(publisher.getNumberOfSubscribers()).isEqualTo(2);
        items.forEach(publisher::submit);
        publisher.close(); // 모든 구독자의 onComplete() 콜백 호출

        await().atMost(1000, TimeUnit.MILLISECONDS)
                .untilAsserted(() ->
                        assertThat(subscriber1.consumedElements).isEqualTo(items)
                );
    }
}
