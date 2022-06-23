package com.example.observable.batched_pull_model;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

/**
 * 대략 2초 정도 걸린다.
 */
public class PullerTest {

    @Test
    public void pullTest() throws InterruptedException {
        CountDownLatch l = new CountDownLatch(1);
        Puller puller = new Puller();

        CompletionStage<Queue<Item>> list = puller.list(10); // 비동기적으로 데이터 10개 생성해서 반환하도록

        // 10개 데이터 반환 받으면 Matcher 이용해서 확인
        list.thenAccept(queue -> {
            MatcherAssert.assertThat(queue, Matchers.allOf(
                    Matchers.hasSize(10),
                    Matchers.contains(
                            Matchers.hasProperty("id", Matchers.equalTo("2")),
                            Matchers.hasProperty("id", Matchers.equalTo("4")),
                            Matchers.hasProperty("id", Matchers.equalTo("6")),
                            Matchers.hasProperty("id", Matchers.equalTo("8")),
                            Matchers.hasProperty("id", Matchers.equalTo("10")),
                            Matchers.hasProperty("id", Matchers.equalTo("12")),
                            Matchers.hasProperty("id", Matchers.equalTo("14")),
                            Matchers.hasProperty("id", Matchers.equalTo("16")),
                            Matchers.hasProperty("id", Matchers.equalTo("18")),
                            Matchers.hasProperty("id", Matchers.equalTo("20"))
                    )
            ));

            // 랫치 카운트를 줄이고 카운트가 0에 도달하면 모든 대기 스레드를 해제
            l.countDown();

        }).exceptionally(throwable -> {
            /**
             * exception이 발생하면 스레드를 해제하고 exception을 throw
             */
            l.countDown();
            throw new RuntimeException(throwable);
        });

        /**
         * 랫치 카운트가 0이 될때까지 스레드를 대기 (list가 비동기이기 때문에 countDown이 발생할 때까지 스레드가 대기한다)
         * 0이 되면 이 await 메소드는 바로 반환된다. 그리고 countDown에 의해서 모든 대기 스레드가 해제된다.
         */
        l.await();
    }
}
