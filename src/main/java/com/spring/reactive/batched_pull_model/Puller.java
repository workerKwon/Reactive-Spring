package com.spring.reactive.batched_pull_model;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Puller {
    final AsyncDatabaseClient dbClient = new DelayedFakeAsyncDatabaseClient();

    public CompletionStage<Queue<Item>> list(int count) {
        BlockingQueue<Item> storage = new ArrayBlockingQueue<>(count);
        CompletableFuture<Queue<Item>> result = new CompletableFuture<>();

        pull("1", storage, result, count);

        return result;
    }

    void pull(String elementId, Queue<Item> queue, CompletableFuture resultFuture, int count) {
        dbClient.getNextBatchAfterId(elementId, count) // CompletableFuture 비동기적으로 데이터 생성
                .thenAccept(items -> {
                    for (Item item : items) {
                        if (isValid(item)) {
                            queue.offer(item);

                            if (queue.size() == count) {
                                resultFuture.complete(queue); // 실제 완성된 데이터 모음 반환.
                                return;
                            }
                        }
                    }

                    /**
                     * 마지막 엘리먼트부터 다시 시작
                     */
                    pull(items.get(items.size() - 1).getId(), queue, resultFuture, count);
                });
    }

    private boolean isValid(Item item) {
        return Integer.parseInt(item.getId()) % 2 == 0;
    }
}
