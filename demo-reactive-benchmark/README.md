# demo-reactive-benchmark

A head-to-head companion to the recorded talk demos, built for the
**"Virtual Threads vs Reactive: when each actually wins"** video.

All three endpoints run the **same workload** — a request fans out to three
downstream dependencies in parallel and aggregates the result. The only thing
that changes is the concurrency model used to wait on that I/O:

| Endpoint             | Model                   | How it waits                                              |
|----------------------|-------------------------|-----------------------------------------------------------|
| `GET /platform/report` | Platform threads        | Blocking calls offloaded to a **bounded** fixed pool (12) |
| `GET /virtual/report`  | Virtual threads (Loom)  | The same blocking calls on a virtual-thread-per-task executor |
| `GET /reactive/report` | Reactive (Reactor)      | Fully non-blocking, `Mono.delay` + `Mono.zip`             |

Each response includes `elapsedMillis`, the `requestThread`, and the
`workerThread` per dependency — so on camera you can show *which kind of thread*
actually did the waiting (a `platform-pool-*` thread, a `VirtualThread`, or a
Netty/parallel scheduler thread).

Downstream latencies (customer 140 ms, ledger 160 ms, inventory 110 ms) mirror
the `demo-virtual-threads` module so numbers line up across the talk.

## Why this is a fair comparison

- **Same fan-out, same latencies.** Only the wait strategy differs.
- **The bottleneck is deliberate.** The platform pool is fixed at 12 threads.
  Under low load all three look identical; the gap only appears once concurrency
  climbs past the pool, which is exactly the story the video tells.
- **Reactive stays non-blocking.** It uses `Mono.delay`, never `Thread.sleep`,
  so the event loop is never parked — the anti-pattern the talk warns about.

The bundled load profile climbs to 10,000 concurrent VUs — three orders of
magnitude past the 12-thread pool — so the platform variant visibly queues while
virtual threads and reactive keep absorbing load.

## Run it

```bash
# from the repo root
mvn -pl demo-reactive-benchmark spring-boot:run
# or run the packaged jar
mvn -pl demo-reactive-benchmark -am package -DskipTests
java -jar demo-reactive-benchmark/target/demo-reactive-benchmark-1.0.0-SNAPSHOT.jar
```

Listens on **http://localhost:8082** (so it can run alongside
`demo-virtual-threads` on 8080 and the dashboard on 8081).

Quick check:

```bash
curl -s 'http://localhost:8082/platform/report?id=7' | jq
curl -s 'http://localhost:8082/virtual/report?id=7'  | jq
curl -s 'http://localhost:8082/reactive/report?id=7' | jq
```

A single request to each is ~160 ms (the slowest dependency), because the
fan-out is parallel in all three models. The differences only emerge **under
load** — see below.

## Load test

See [`../load/k6-benchmark.js`](../load/k6-benchmark.js):

```bash
k6 run -e VARIANT=platform load/k6-benchmark.js
k6 run -e VARIANT=virtual  load/k6-benchmark.js
k6 run -e VARIANT=reactive load/k6-benchmark.js
```

Compare `http_reqs` (throughput) and `http_req_duration` p95/p99 across the
three runs. Add `--summary-export=benchmarks/<variant>-summary.json` to capture
the numbers for a chart.

## What to show on screen

- Throughput and p95/p99 for each variant at the same concurrency level.
- The `workerThread` field flipping between `platform-pool-*`, `VirtualThread`,
  and the reactive scheduler.
- A reminder that downstream latency still dominates a *single* request — the
  models differ in how they **scale**, not in best-case latency.
