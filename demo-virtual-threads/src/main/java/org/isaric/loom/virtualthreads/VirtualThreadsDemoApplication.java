package org.isaric.loom.virtualthreads;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;

public final class VirtualThreadsDemoApplication {
    private static final int PORT = 8080;
    private static final ExecutorService BASELINE_EXECUTOR = Executors.newFixedThreadPool(12);
    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private static final ReportService REPORT_SERVICE = new ReportService();

    private VirtualThreadsDemoApplication() {
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/health", exchange -> writeResponse(exchange, 200, "{\"status\":\"ok\"}"));
        server.createContext("/baseline/report", exchange -> handle(exchange, Mode.BASELINE));
        server.createContext("/virtual/report", exchange -> handle(exchange, Mode.VIRTUAL));
        server.createContext("/structured/report", exchange -> handle(exchange, Mode.STRUCTURED));
        server.setExecutor(Executors.newCachedThreadPool());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            BASELINE_EXECUTOR.shutdown();
            VIRTUAL_EXECUTOR.shutdown();
            server.stop(0);
        }));
        System.out.println("Virtual threads demo listening on http://localhost:" + PORT);
        System.out.println("Try /baseline/report, /virtual/report, and /structured/report");
        server.start();
    }

    private static void handle(HttpExchange exchange, Mode mode) throws IOException {
        String itemId = queryParam(exchange.getRequestURI(), "id", "42");
        Instant startedAt = Instant.now();
        Callable<ReportPayload> action = switch (mode) {
            case BASELINE -> () -> REPORT_SERVICE.buildSequential(itemId, "fixed-pool");
            case VIRTUAL -> () -> REPORT_SERVICE.buildSequential(itemId, "virtual-thread");
            case STRUCTURED -> () -> REPORT_SERVICE.buildStructured(itemId);
        };

        try {
            ReportPayload payload = switch (mode) {
                case BASELINE -> BASELINE_EXECUTOR.submit(action).get();
                case VIRTUAL -> VIRTUAL_EXECUTOR.submit(action).get();
                case STRUCTURED -> action.call();
            };
            String response = payload.toJson(Duration.between(startedAt, Instant.now()).toMillis(), mode.name().toLowerCase());
            writeResponse(exchange, 200, response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            writeResponse(exchange, 500, errorJson("interrupted", e));
        } catch (ExecutionException e) {
            writeResponse(exchange, 500, errorJson("request failed", e.getCause()));
        } catch (Exception e) {
            writeResponse(exchange, 500, errorJson("request failed", e));
        }
    }

    private static String queryParam(URI uri, String name, String fallback) {
        String query = uri.getRawQuery();
        if (query == null || query.isBlank()) {
            return fallback;
        }
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && parts[0].equals(name)) {
                return parts[1];
            }
        }
        return fallback;
    }

    private static void writeResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private static String errorJson(String message, Throwable throwable) {
        String details = throwable == null ? "unknown" : throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
        return "{\"error\":\"" + message + "\",\"details\":\"" + escape(details) + "\"}";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private enum Mode {
        BASELINE,
        VIRTUAL,
        STRUCTURED
    }

    private static final class ReportService {
        ReportPayload buildSequential(String itemId, String executionModel) throws InterruptedException {
            String customer = simulateLatency("customer-service", itemId, 140);
            String ledger = simulateLatency("ledger-service", itemId, 160);
            String inventory = simulateLatency("inventory-db", itemId, 110);
            return new ReportPayload(itemId, executionModel, customer, ledger, inventory, Thread.currentThread().toString());
        }

        ReportPayload buildStructured(String itemId) throws Exception {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                var customer = scope.fork(() -> simulateLatency("customer-service", itemId, 140));
                var ledger = scope.fork(() -> simulateLatency("ledger-service", itemId, 160));
                var inventory = scope.fork(() -> simulateLatency("inventory-db", itemId, 110));
                scope.join().throwIfFailed();
                return new ReportPayload(
                        itemId,
                        "structured-concurrency",
                        customer.get(),
                        ledger.get(),
                        inventory.get(),
                        Thread.currentThread().toString());
            }
        }

        private String simulateLatency(String dependency, String itemId, long millis) throws InterruptedException {
            Thread.sleep(millis);
            return dependency + "-result-for-" + itemId + "@" + Thread.currentThread().getName();
        }
    }

    private record ReportPayload(
            String itemId,
            String executionModel,
            String customer,
            String ledger,
            String inventory,
            String requestThread) {
        String toJson(long elapsedMillis, String endpoint) {
            Map<String, String> values = new LinkedHashMap<>();
            values.put("endpoint", endpoint);
            values.put("itemId", itemId);
            values.put("executionModel", executionModel);
            values.put("elapsedMillis", Long.toString(elapsedMillis));
            values.put("requestThread", requestThread);
            values.put("customer", customer);
            values.put("ledger", ledger);
            values.put("inventory", inventory);

            StringBuilder builder = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, String> entry : values.entrySet()) {
                if (!first) {
                    builder.append(',');
                }
                builder.append('"').append(entry.getKey()).append('"').append(':').append('"').append(escape(entry.getValue())).append('"');
                first = false;
            }
            builder.append('}');
            return builder.toString();
        }
    }
}
