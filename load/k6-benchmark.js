// k6 load test for the platform vs virtual vs reactive head-to-head.
//
// Runs the SAME ramping profile against each endpoint so you can put the three
// result sets side by side on screen. The ramp climbs past the platform pool's
// 12 threads on purpose: that is where the thread-per-task model starts queuing
// while virtual threads and reactive keep absorbing load.
//
// Usage:
//   1. Start the app:   mvn -pl demo-reactive-benchmark spring-boot:run
//   2. Pick a variant:  k6 run -e VARIANT=platform load/k6-benchmark.js
//                       k6 run -e VARIANT=virtual  load/k6-benchmark.js
//                       k6 run -e VARIANT=reactive load/k6-benchmark.js
//   3. Compare http_req_duration p95/p99 and http_reqs/s across the three runs.
//
// Tip for the recording: export the summary with
//   k6 run --summary-export=benchmarks/<variant>.json -e VARIANT=<variant> load/k6-benchmark.js

import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';

const VARIANT = __ENV.VARIANT || 'reactive';
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8082';

// Server-reported aggregation time, separate from k6's client-side http_req_duration.
const serverElapsed = new Trend('server_elapsed_ms', true);

export const options = {
  scenarios: {
    ramp: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '20s', target: 100 },    // warm up + JIT
        { duration: '30s', target: 1000 },
        { duration: '30s', target: 10000 },   // past the 12-thread pool by 3 orders of magnitude
        { duration: '20s', target: 10000 },   // hold
        { duration: '10s', target: 0 },       // ramp down
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    // Intentionally lenient bounds so a normal run stays green and the p95/p99 + failure
    // rate get surfaced in the summary. Note: if a threshold IS breached, k6 marks the run
    // failed (non-zero exit) — it just won't abort the scenario early.
    http_req_duration: ['p(95)<5000', 'p(99)<10000'],
    http_req_failed: ['rate<0.05'],
  },
};

export default function () {
  const res = http.get(`${BASE_URL}/${VARIANT}/report?id=${__VU}`);
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
  if (res.status === 200) {
    try {
      const body = res.json();
      if (body && typeof body.elapsedMillis === 'number') {
        serverElapsed.add(body.elapsedMillis);
      }
    } catch (_) {
      // ignore parse errors under heavy load
    }
  }
}

export function handleSummary(data) {
  const reqs = data.metrics.http_reqs ? data.metrics.http_reqs.values.count : 0;
  const rate = data.metrics.http_reqs ? data.metrics.http_reqs.values.rate : 0;
  const dur = data.metrics.http_req_duration ? data.metrics.http_req_duration.values : {};
  const failed = data.metrics.http_req_failed ? data.metrics.http_req_failed.values.rate : 0;
  const line = [
    `\nVariant:            ${VARIANT}`,
    `Total requests:     ${reqs}`,
    `Throughput:         ${rate.toFixed(1)} req/s`,
    `Latency p50:        ${(dur['p(50)'] || 0).toFixed(1)} ms`,
    `Latency p95:        ${(dur['p(95)'] || 0).toFixed(1)} ms`,
    `Latency p99:        ${(dur['p(99)'] || 0).toFixed(1)} ms`,
    `Failed:             ${(failed * 100).toFixed(2)} %`,
    '',
  ].join('\n');
  return {
    stdout: line,
    [`benchmarks/${VARIANT}-summary.json`]: JSON.stringify(data, null, 2),
  };
}
