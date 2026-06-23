# Benchmark notes

These demos are intentionally lightweight and are meant for a recorded talk, not a scientific benchmark suite.

## Demo 1 quick comparison

Use the HTTP demo and compare these endpoints under the same client-side concurrency level:

- `/baseline/report`
- `/virtual/report`
- `/structured/report`

Example load command:

```bash
seq 1 40 | xargs -n1 -P40 -I{} curl -s 'http://localhost:8080/virtual/report?id={}' > /dev/null
```

## What to show on screen

- throughput under the same concurrency level
- p95/p99 latency if your load tool reports it
- thread names from the JSON payloads
- reminder that downstream latency still dominates the total time

## Demo 2 head-to-head: platform vs virtual vs reactive

The `demo-reactive-benchmark` module (Spring Boot + WebFlux, port 8082) runs the
**same parallel fan-out workload** three ways so you can compare them directly:

- `/platform/report` — blocking calls on a bounded fixed pool (12 threads)
- `/virtual/report`  — the same blocking calls on virtual threads
- `/reactive/report` — fully non-blocking (`Mono.delay` + `Mono.zip`)

Load test all three with the bundled k6 script (ramps to 10k VUs, well past the
12-thread pool):

```bash
k6 run -e VARIANT=platform load/k6-benchmark.js
k6 run -e VARIANT=virtual  load/k6-benchmark.js
k6 run -e VARIANT=reactive load/k6-benchmark.js
```

See [`../demo-reactive-benchmark/README.md`](../demo-reactive-benchmark/README.md)
for the full rationale and what to show on screen.

## Sample chart input

The CSV in this folder gives you a starter dataset for a slide mock-up before you collect real numbers on the recording machine.
