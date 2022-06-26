package com.example.news_service;

import com.example.news_service.dto.NewsLetter;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class SmartMulticastProcessor implements Processor<NewsLetter, NewsLetter> {

    private static final InnerSubscription[] EMPTY = new InnerSubscription[0];
    private static final InnerSubscription[] TERMINATED = new InnerSubscription[0];

    Throwable throwable;
    NewsLetter cacheNewsLetter;

    volatile     InnerSubscription[] active = EMPTY;
    static final AtomicReferenceFieldUpdater<SmartMulticastProcessor, InnerSubscription[]> ACTIVE =
            AtomicReferenceFieldUpdater.newUpdater(SmartMulticastProcessor.class, InnerSubscription[].class, "active");

    @Override
    public void subscribe(Subscriber<? super NewsLetter> actual) {
        Objects.requireNonNull(actual);


    }

    @Override
    public void onSubscribe(Subscription s) {

    }

    @Override
    public void onNext(NewsLetter newsLetter) {

    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onComplete() {

    }

    boolean isTerminated() {
        return active == TERMINATED;
    }

    private static class InnerSubscription implements Subscription {
        final Subscriber<? super NewsLetter> actual;
        final SmartMulticastProcessor parent;

        Throwable throwable;
        boolean done;
        boolean sent;

        /**
         * volatile
         * - Main Memory에 저장하겠다.
         * - Read할 때마다 CPU cache 값이 아닌 Main Memory에서 읽는다.
         * - Write할 때에도 Main Memory에 까지 작성하는 것.
         */

        volatile     long                                      requested;
        static final AtomicLongFieldUpdater<InnerSubscription> REQUESTED =
                AtomicLongFieldUpdater.newUpdater(InnerSubscription.class, "requested");
        volatile int wip;
        static final AtomicIntegerFieldUpdater<InnerSubscription> WIP = AtomicIntegerFieldUpdater.newUpdater(InnerSubscription.class, "wip");

        public InnerSubscription(Subscriber<? super NewsLetter> actual, SmartMulticastProcessor parent) {
            this.actual = actual;
            this.parent = parent;
        }

        @Override
        public void request(long n) {
            if (n <= 0) {
                onError(throwable = new IllegalArgumentException("negative subscription request"));
                return;
            }
            // TODO 진행
        }

        @Override
        public void cancel() {

        }

        void onError(Throwable t) {
            if (done) {
                return;
            }
            tryDrain();
        }

        void tryDrain() {
            if(done) {
                return;
            }

            int wip;

            if ((wip = WIP.incrementAndGet(this)) > 1) {
                return;
            }

            Subscriber<? super NewsLetter> actualSubscriber = actual;
            long req = requested;

            for (;;) {
                NewsLetter element = parent.cacheNewsLetter;

                /**
                 * request가 있고 sent가 false이고 NewsLetter가 있으면
                 */
                if (req > 0 && !sent && element != null) {
                    actualSubscriber.onNext(element.withRecipient(getRecipient()));
                    sent = true;

                    req = REQUESTED.decrementAndGet(this);
                }

                wip = WIP.addAndGet(this, -wip);

                if (wip == 0) {
                    if (!done && isTerminated() && hasNoMoreEmission()) {
                        done = true;
                        if (throwable == null && parent.throwable == null) {
                            actualSubscriber.onComplete();
                        } else {
                            throwable = throwable == null ? parent.throwable : throwable;
                            actualSubscriber.onError(throwable);
                        }
                    }

                    return;
                }
            }
        }

        boolean isTerminated() {
            return parent.throwable != null || parent.isTerminated();
        }

        boolean hasNoMoreEmission() {
            return sent || parent.cacheNewsLetter == null || throwable != null;
        }

        String getRecipient() {
            if (actual instanceof NamedSubscriber) {
                return ((NamedSubscriber) actual).getName();
            }
            return null;
        }
    }
}
