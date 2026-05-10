package org.isaric.loom.hybrid;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class HybridReactiveDemoApplication {
    private static final int MAX_IN_FLIGHT = 3;
    private static final int EVENT_COUNT = 12;

    private HybridReactiveDemoApplication() {
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch finished = new CountDownLatch(1);
        try (ExecutorService businessExecutor = Executors.newVirtualThreadPerTaskExecutor();
             SubmissionPublisher<OrderEvent> ingress = new SubmissionPublisher<>()) {
            BusinessSubscriber subscriber = new BusinessSubscriber(MAX_IN_FLIGHT, businessExecutor, finished);
            ingress.subscribe(subscriber);

            log("main", "publishing " + EVENT_COUNT + " events into the reactive edge");
            for (int index = 1; index <= EVENT_COUNT; index++) {
                if (subscriber.isClosed()) {
                    log("main", "stopping publisher because the pipeline has terminated");
                    break;
                }
                OrderEvent event = new OrderEvent(index, "customer-" + ((index % 4) + 1));
                log("publisher", "submitted " + event);
                ingress.submit(event);
                Thread.sleep(40);
            }
            ingress.close();
            finished.await();
        }
        log("main", "hybrid demo complete");
    }

    private static void log(String stage, String message) {
        System.out.printf("[%s] %-10s %s (%s)%n", Instant.now(), stage, message, threadLabel());
    }

    private static String threadLabel() {
        String name = Thread.currentThread().getName();
        return name == null || name.isBlank() ? Thread.currentThread().toString() : name;
    }

    private record OrderEvent(int id, String customerId) {
        @Override
        public String toString() {
            return "OrderEvent{id=" + id + ", customerId='" + customerId + "'}";
        }
    }

    private record ProcessedEvent(OrderEvent source, Duration elapsed) {
        @Override
        public String toString() {
            return "ProcessedEvent{id=" + source.id() + ", elapsedMs=" + elapsed.toMillis() + "}";
        }
    }

    private static final class BusinessSubscriber implements Flow.Subscriber<OrderEvent> {
        private final int maxInFlight;
        private final ExecutorService businessExecutor;
        private final CountDownLatch finished;
        private final AtomicBoolean closed = new AtomicBoolean();
        private final AtomicInteger inFlight = new AtomicInteger();
        private Flow.Subscription subscription;

        private BusinessSubscriber(int maxInFlight, ExecutorService businessExecutor, CountDownLatch finished) {
            this.maxInFlight = maxInFlight;
            this.businessExecutor = businessExecutor;
            this.finished = finished;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            log("subscriber", "subscribed; requesting " + maxInFlight + " items");
            subscription.request(maxInFlight);
        }

        @Override
        public void onNext(OrderEvent item) {
            if (closed.get()) {
                cancelSubscription();
                return;
            }
            int current = inFlight.incrementAndGet();
            log("subscriber", "accepted " + item + "; in-flight=" + current);
            try {
                businessExecutor.submit(() -> {
                    Throwable failure = null;
                    try {
                        ProcessedEvent processed = processBlocking(item);
                        if (!closed.get()) {
                            log("sink", "emitted " + processed);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        failure = e;
                    } catch (RuntimeException e) {
                        failure = e;
                    } finally {
                        int remaining = inFlight.decrementAndGet();
                        if (failure != null) {
                            failPipeline(failure);
                        } else if (!closed.get()) {
                            log("subscriber", "requesting next item; in-flight now=" + remaining);
                            subscription.request(1);
                        }
                    }
                });
            } catch (RejectedExecutionException e) {
                int remaining = inFlight.decrementAndGet();
                log("subscriber", "worker submission rejected; in-flight now=" + remaining);
                failPipeline(e);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            failPipeline(throwable);
        }

        @Override
        public void onComplete() {
            if (closed.get()) {
                return;
            }
            log("subscriber", "publisher completed; waiting for in-flight work");
            try {
                businessExecutor.submit(() -> {
                    while (!closed.get() && inFlight.get() > 0) {
                        try {
                            Thread.sleep(25);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            failPipeline(e);
                            return;
                        }
                    }
                    if (closed.compareAndSet(false, true)) {
                        log("subscriber", "all business work finished");
                        finished.countDown();
                    }
                });
            } catch (RejectedExecutionException e) {
                failPipeline(e);
            }
        }

        private boolean isClosed() {
            return closed.get();
        }

        private void failPipeline(Throwable throwable) {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            cancelSubscription();
            String message = throwable == null || throwable.getMessage() == null
                    ? throwable == null ? "unknown failure" : throwable.getClass().getSimpleName()
                    : throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
            log("subscriber", "pipeline failed: " + message);
            finished.countDown();
        }

        private void cancelSubscription() {
            if (subscription != null) {
                subscription.cancel();
            }
        }

        private ProcessedEvent processBlocking(OrderEvent item) throws InterruptedException {
            Instant startedAt = Instant.now();
            log("business", "start blocking work for order " + item.id());
            Thread.sleep(120);
            Thread.sleep(90);
            log("business", "finished blocking work for order " + item.id());
            return new ProcessedEvent(item, Duration.between(startedAt, Instant.now()));
        }
    }
}
