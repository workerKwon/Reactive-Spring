package com.example.pub_sub;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ControllSubscriberTest {

    @Test
    public void whenRequestForOnlyOneElement_thenShouldConsumeOne() {
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
        ControllSubscriber<Object> subscriber1 = new ControllSubscriber<>(1);
        ControllSubscriber<Object> subscriber2 = new ControllSubscriber<>(1);
        publisher.subscribe(subscriber1);
        publisher.subscribe(subscriber2);
        List<String> items = List.of("1", "x", "2", "x", "3", "x");
        List<String> expected = List.of("1");

        assertThat(publisher.getNumberOfSubscribers()).isEqualTo(2);
        items.forEach(publisher::submit);
        publisher.close();

        await().atMost(1000, TimeUnit.MILLISECONDS)
                .untilAsserted(() ->
                        assertThat(subscriber1.consumedElements)
                                .containsExactlyElementsOf(expected)
                );
    }

    @Test
    public void whenRequestForTwoElement_thenShouldConsumeTwo() {
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
        ControllSubscriber<Object> subscriber = new ControllSubscriber<>(2);
        publisher.subscribe(subscriber);
        List<String> items = List.of("1", "x", "2", "x", "3", "x");
        List<String> expected = List.of("1","x");

        assertThat(publisher.getNumberOfSubscribers()).isEqualTo(1);
        items.forEach(publisher::submit);
        publisher.close();

        await().atMost(1000, TimeUnit.MILLISECONDS)
                .untilAsserted(() ->
                        assertThat(subscriber.consumedElements)
                                .containsExactlyElementsOf(expected)
                );
    }
}
