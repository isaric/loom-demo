# Loom demo projects

This repository turns the recorded-talk package in [`Project Loom - Copilot Package.md`](./Project%20Loom%20-%20Copilot%20Package.md) into runnable demo assets.

## Prerequisites

- JDK 25+
- Maven 3.9+

## Projects

- [`demo-virtual-threads`](./demo-virtual-threads): HTTP demo with baseline, virtual-thread, and structured-concurrency endpoints.
- [`demo-hybrid-reactive`](./demo-hybrid-reactive): console demo that keeps a reactive edge with explicit backpressure and hands blocking work to virtual threads.
- [`benchmarks`](./benchmarks): lightweight benchmark notes and sample result data for the recorded talk.
- [`slides`](./slides): slide/demo mapping notes for recording prep.

## Build everything

```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
mvn verify
```

## Run demo 1: virtual threads and structured concurrency

```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
mvn -pl demo-virtual-threads exec:java
```

Then exercise the endpoints in a second terminal:

```bash
curl 'http://localhost:8080/health'
curl 'http://localhost:8080/baseline/report?id=42'
curl 'http://localhost:8080/virtual/report?id=42'
curl 'http://localhost:8080/structured/report?id=42'
```

For a quick load sample during recording:

```bash
seq 1 20 | xargs -n1 -P20 -I{} curl -s 'http://localhost:8080/virtual/report?id={}' > /dev/null
```

## Run demo 2: hybrid reactive edge + virtual-thread workers

```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
mvn -pl demo-hybrid-reactive exec:java
```

The console output shows:

- publisher/backpressure behavior at the reactive edge
- bounded in-flight demand
- blocking business work running on virtual threads
- downstream result emission after the imperative stage completes
