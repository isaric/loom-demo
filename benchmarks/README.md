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

## Sample chart input

The CSV in this folder gives you a starter dataset for a slide mock-up before you collect real numbers on the recording machine.
