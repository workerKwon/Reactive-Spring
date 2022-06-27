package com.example.news_service;

import com.example.news_service.dto.NewsLetter;
import javassist.runtime.Inner;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * 최신뉴스 요약본을 캐싱한다. 멀티 캐스트를 지원하므로 구독자별로 동일한 흐름을 별도로 만들 필요가 없이 한번에 처리 가능하다.
 * 스마트 메일 추적 매커니즘으로 이전 다이제스트를 읽은 사용자에게만 뉴스레터를 보낸다.
 * 실제 구독자가 SmartMuSmartMulticastProcessor를 구독하고 SmartMulticastProcessor는 Scheduler를 구독한다.
 */
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
        InnerSubscription subscription = new InnerSubscription(actual, this);

        /**
         * 구독 추가가 성공/실패
         */
        if (add(subscription)) {
            actual.onSubscribe(subscription);
        } else {
            actual.onSubscribe(subscription);

            if(throwable == null) {
                actual.onComplete();
            } else {
                actual.onError(throwable);
            }
        }
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

    private boolean add(InnerSubscription subscription) {
        for (;;) {
            InnerSubscription[] subscriptions = active;
            if (isTerminated()) {
                return false;
            }

            int n = subscriptions.length;
            InnerSubscription[] copied = new InnerSubscription[n + 1];

            if (n > 0) {
                int index = (n-1) & hash(subscription);
                if (subscriptions[index].equals(subscription)) { // 새로운 구독이 이미 있는 구독이라면 false
                    return false;
                }

                System.arraycopy(subscriptions, 0, copied, 0, n);
            }
            copied[n] = subscription;

            if (ACTIVE.compareAndSet(this, subscriptions, copied)) {
                return true;
            }
        }
    }

    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
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
