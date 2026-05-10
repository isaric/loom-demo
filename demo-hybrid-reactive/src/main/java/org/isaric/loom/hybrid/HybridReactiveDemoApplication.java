package org.isaric.loom.hybrid;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
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
            ingress.subscribe(new BusinessSubscriber(MAX_IN_FLIGHT, businessExecutor, finished));

            log("main", "publishing " + EVENT_COUNT + " events into the reactive edge");
            for (int index = 1; index <= EVENT_COUNT; index++) {
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
            int current = inFlight.incrementAndGet();
            log("subscriber", "accepted " + item + "; in-flight=" + current);
            businessExecutor.submit(() -> {
                try {
                    ProcessedEvent processed = processBlocking(item);
                    log("sink", "emitted " + processed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    onError(e);
                    return;
                }
                int remaining = inFlight.decrementAndGet();
                log("subscriber", "requesting next item; in-flight now=" + remaining);
                subscription.request(1);
            });
        }

        @Override
        public void onError(Throwable throwable) {
            log("subscriber", "pipeline failed: " + throwable.getMessage());
            finished.countDown();
        }

        @Override
        public void onComplete() {
            log("subscriber", "publisher completed; waiting for in-flight work");
            businessExecutor.submit(() -> {
                while (inFlight.get() > 0) {
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                log("subscriber", "all business work finished");
                finished.countDown();
            });
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
