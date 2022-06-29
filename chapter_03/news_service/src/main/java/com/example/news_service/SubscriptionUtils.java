package com.example.news_service;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public class SubscriptionUtils {
    private SubscriptionUtils() {}

    public static long addCap(long current, long requested) {
        long cap = current + requested;

        if (cap < 0L) {
            cap = Long.MAX_VALUE;
        }

        return cap;
    }

    public static long request(long n, Object instance, AtomicLongFieldUpdater updater) {
        for (;;) {
            long currentDemand = updater.get(instance);

            if (currentDemand == Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }

            long adjustedDemand = addCap(currentDemand, n);

            if (updater.compareAndSet(instance, currentDemand, adjustedDemand)) {
                return currentDemand;
            }
        }
    }
}
