package com.example.pub_sub;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.*;

public class TransformProcessorTest {
    @Test
    public void whenSubscribeAndTransformElements_thenSShouldConsumeAll() {
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
        TransformProcessor<String, Integer> processor = new TransformProcessor<>(Integer::parseInt);
        EndSubscriber<Object> subscriber1 = new EndSubscriber<>();
        EndSubscriber<Object> subscriber2 = new EndSubscriber<>();
        List<String> items = List.of("1", "2", "3");
        List<Integer> expectedResult = List.of(1, 2, 3);

        publisher.subscribe(processor);
        processor.subscribe(subscriber1);
        processor.subscribe(subscriber2);
        items.forEach(publisher::submit);
        publisher.close();

        await().atMost(1000, TimeUnit.MILLISECONDS)
                .untilAsserted(() ->
                                assertThat(subscriber1.consumedElements)
                                        .containsExactlyElementsOf(expectedResult)
                );
    }
}
