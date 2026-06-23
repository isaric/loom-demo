package org.isaric.loom.benchmark;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Head-to-head companion to the recorded talk demos.
 *
 * <p>All three endpoints run the <em>same</em> workload: a request fans out to three downstream
 * dependencies in parallel and aggregates the result. The only thing that changes is the
 * concurrency model used to wait on that I/O:
 *
 * <ul>
 *   <li>{@code /platform/report} - blocking calls offloaded to a bounded platform-thread pool
 *       (classic thread-per-task).</li>
 *   <li>{@code /virtual/report} - the same blocking calls offloaded to a virtual-thread-per-task
 *       executor (Loom).</li>
 *   <li>{@code /reactive/report} - fully non-blocking, latency modelled with {@link Mono#delay}
 *       and composed with {@code Mono.zip} (Reactor).</li>
 * </ul>
 *
 * <p>Downstream latencies (customer 140ms, ledger 160ms, inventory 110ms) mirror the
 * {@code demo-virtual-threads} module so numbers are comparable across the talk.
 */
@SpringBootApplication
@RestController
public class ReactiveBenchmarkApplication {

    private static final long CUSTOMER_LATENCY_MS = 140;
    private static final long LEDGER_LATENCY_MS = 160;
    private static final long INVENTORY_LATENCY_MS = 110;

    /** Bounded pool: the deliberate bottleneck that makes the platform-thread model fall over under load. */
    private final ExecutorService platformPool = Executors.newFixedThreadPool(12, namedFactory("platform-pool"));
    private final Scheduler platformScheduler = Schedulers.fromExecutorService(platformPool);

    private final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final Scheduler virtualScheduler = Schedulers.fromExecutorService(virtualExecutor);

    public static void main(String[] args) {
        SpringApplication.run(ReactiveBenchmarkApplication.class, args);
    }

    @GetMapping("/health")
    Mono<Map<String, Object>> health() {
        return Mono.just(Map.of("status", "ok"));
    }

    @GetMapping(value = "/platform/report", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<Map<String, Object>> platform(@RequestParam(name = "id", defaultValue = "42") String id) {
        return blockingFanOut(id, "platform-threads", platformScheduler);
    }

    @GetMapping(value = "/virtual/report", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<Map<String, Object>> virtual(@RequestParam(name = "id", defaultValue = "42") String id) {
        return blockingFanOut(id, "virtual-threads", virtualScheduler);
    }

    @GetMapping(value = "/reactive/report", produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<Map<String, Object>> reactive(@RequestParam(name = "id", defaultValue = "42") String id) {
        return Mono.defer(() -> {
            Instant start = Instant.now();
            Mono<Dependency> customer = reactiveCall("customer-service", id, CUSTOMER_LATENCY_MS);
            Mono<Dependency> ledger = reactiveCall("ledger-service", id, LEDGER_LATENCY_MS);
            Mono<Dependency> inventory = reactiveCall("inventory-db", id, INVENTORY_LATENCY_MS);
            return Mono.zip(customer, ledger, inventory)
                    .map(results -> report(id, "reactive", start, results.getT1(), results.getT2(), results.getT3()));
        });
    }

    /**
     * Runs the three dependencies in parallel as blocking calls, each offloaded to {@code scheduler}.
     * Identical business logic for the platform and virtual variants - only the scheduler differs.
     */
    private Mono<Map<String, Object>> blockingFanOut(String id, String executionModel, Scheduler scheduler) {
        return Mono.defer(() -> {
            Instant start = Instant.now();
            Mono<Dependency> customer = blockingCall("customer-service", id, CUSTOMER_LATENCY_MS).subscribeOn(scheduler);
            Mono<Dependency> ledger = blockingCall("ledger-service", id, LEDGER_LATENCY_MS).subscribeOn(scheduler);
            Mono<Dependency> inventory = blockingCall("inventory-db", id, INVENTORY_LATENCY_MS).subscribeOn(scheduler);
            return Mono.zip(customer, ledger, inventory)
                    .map(results -> report(id, executionModel, start, results.getT1(), results.getT2(), results.getT3()));
        });
    }

    /** A blocking downstream call - the kind of code virtual threads let you keep writing. */
    private Mono<Dependency> blockingCall(String dependency, String id, long latencyMs) {
        return Mono.fromCallable(() -> {
            Thread.sleep(latencyMs);
            return new Dependency(dependency + "-result-for-" + id, Thread.currentThread().toString());
        });
    }

    /** A non-blocking downstream call - no carrier thread is held while we "wait". */
    private Mono<Dependency> reactiveCall(String dependency, String id, long latencyMs) {
        return Mono.delay(Duration.ofMillis(latencyMs))
                .map(tick -> new Dependency(dependency + "-result-for-" + id, Thread.currentThread().toString()));
    }

    private Map<String, Object> report(String id, String executionModel, Instant start,
                                       Dependency customer, Dependency ledger, Dependency inventory) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executionModel", executionModel);
        body.put("itemId", id);
        body.put("elapsedMillis", Duration.between(start, Instant.now()).toMillis());
        body.put("requestThread", Thread.currentThread().toString());
        body.put("customer", customer.toMap());
        body.put("ledger", ledger.toMap());
        body.put("inventory", inventory.toMap());
        return body;
    }

    private static java.util.concurrent.ThreadFactory namedFactory(String prefix) {
        java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(prefix + "-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    private record Dependency(String value, String workerThread) {
        Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("value", value);
            map.put("workerThread", workerThread);
            return map;
        }
    }
}
