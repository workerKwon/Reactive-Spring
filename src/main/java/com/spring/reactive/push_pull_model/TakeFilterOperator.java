package com.spring.reactive.push_pull_model;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.Predicate;

public class TakeFilterOperator<T> implements Publisher<T> {

    private final Publisher<T> source;
    private final int take;
    private final Predicate<T> predicate;

    public TakeFilterOperator(Publisher<T> source, int take, Predicate<T> predicate) {
        this.source = source;
        this.take = take;
        this.predicate = predicate;
    }

    @Override
    public void subscribe(Subscriber s) {
        /**
         * dbClient(데이터 제공 Publisher)의 구독 시작
         * s - 실제 subscriber
         */
        source.subscribe(new TakeFilterInner<>(s, take, predicate));
    }

    /**
     * 실제 subscriber의 요구사항을 제어하면서 또한 실제 Subscriber로서 메인 소스에 전달되는 역할
     * @param <T>
     */
    static final class TakeFilterInner<T> implements Subscriber<T>, Subscription {

        final Subscriber<T> actual;
        final int take;
        final Predicate<T> predicate;
        final Queue<T> queue;

        Subscription current;
        int remaining;
        int filtered;
        Throwable throwable;
        boolean done;

        volatile long requested;
        static final AtomicLongFieldUpdater<TakeFilterInner> REQUESTED =
                AtomicLongFieldUpdater.newUpdater(TakeFilterInner.class, "requested");

        volatile int wip;
        static final AtomicIntegerFieldUpdater<TakeFilterInner> WIP =
                AtomicIntegerFieldUpdater.newUpdater(TakeFilterInner.class, "wip");

        TakeFilterInner(Subscriber<T> actual, int take, Predicate<T> predicate) {
            this.actual = actual;
            this.take = take;
            this.remaining = take;
            this.predicate = predicate;
            this.queue = new ConcurrentLinkedQueue<>();
        }

        @Override
        public void onSubscribe(Subscription current) {
            if (this.current == null) {
                this.current = current;

                this.actual.onSubscribe(this);
                if (take > 0) {
                    this.current.request(take);
                } else {
                    onComplete();
                }
            } else {
                current.cancel();
            }
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }

            long r = requested;
            Subscriber<T> a = actual;
            Subscription s = current;

            if (remaining > 0) {
                boolean isValid = predicate.test(t);
                boolean isEmpty = queue.isEmpty();

                if (isValid && r > 0 && isEmpty) {
                    /**
                     * a 는 publisher에 직접 구독한 subscriber. subscriber한테 item을 넘겨줘서 subscriber.List에 담는다.
                     */
                    a.onNext(t);
                    remaining--;

                    REQUESTED.decrementAndGet(this);
                    if (remaining == 0) {
                        s.cancel();
                        onComplete();
                    }
                } else if (isValid && (r == 0 || !isEmpty)) {
                    queue.offer(t);
                    remaining--;

                    if (remaining == 0) {
                        s.cancel();
                        onComplete();
                    }
                    drain(a, r);
                } else if (!isValid) {
                    filtered++;
                }
            } else {
                s.cancel();
                onComplete();
            }

            if (filtered > 0 && remaining / filtered < 2) {
                s.request(take);
                filtered = 0;
            }
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                return;
            }

            done = true;

            if (queue.isEmpty()) {
                actual.onError(t);
            } else {
                throwable = t;
            }
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }

            done = true;

            if (queue.isEmpty()) {
                actual.onComplete();
            }
        }


        @Override
        public void request(long n) {
            if (n <= 0) {
                onError(new IllegalArgumentException(
                        "Spec. Rule 3.9 - Cannot request a non strictly positive number: " + n
                ));
            }

            drain(actual, SubscriptionUtils.request(n, this, REQUESTED));
        }

        @Override
        public void cancel() {
            if (!done) {
                current.cancel();
            }

            queue.clear();
        }

        void drain(Subscriber<T> a, long r) {
            if (queue.isEmpty() || r == 0) {
                return;
            }

            int wip;

            if ((wip = WIP.incrementAndGet(this)) > 1) {
                return;
            }

            int c = 0;
            boolean empty;

            for (;;) {
                T e;
                while (c != r && (e = queue.poll()) != null) {
                    a.onNext(e);
                    c++;
                }


                empty = queue.isEmpty();
                r = REQUESTED.addAndGet(this, -c);
                c = 0;

                if (r == 0 || empty) {
                    if (done && empty) {
                        if (throwable == null) {
                            a.onComplete();
                        }
                        else {
                            a.onError(throwable);
                        }
                        return;
                    }

                    wip = WIP.addAndGet(this, -wip);

                    if (wip == 0) {
                        return;
                    }
                }
                else {
                    wip = this.wip;
                }
            }
        }
    }
}
