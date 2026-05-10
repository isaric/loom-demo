# Recording asset notes

Use the repository assets alongside the sections in `Project Loom - Copilot Package.md`.

## Demo 1 mapping

- Slide 8: explain the service shape with `demo-virtual-threads`
- Slide 9: show the three endpoints and compare the JSON payloads
- Slide 10: use `benchmarks/virtual-threads-sample-results.csv` as a placeholder chart source

## Demo 2 mapping

- Slide 14: explain the reactive-edge / imperative-worker boundary
- Slide 15: run `demo-hybrid-reactive` and narrate the backpressure requests and virtual-thread worker logs

## Suggested screenshots to capture before recording

- `curl http://localhost:8080/virtual/report?id=42`
- `curl http://localhost:8080/structured/report?id=42`
- one short terminal capture from `demo-hybrid-reactive`
- one simple bar chart generated from the benchmark CSV
