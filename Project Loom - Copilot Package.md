# Outline

Yes — here’s a solid recorded-talk package you can use.

## Recorded talk package

### 1\) Submission-ready abstract

Java’s concurrency story changed dramatically with virtual threads, but the real question for teams in 2026 is not “Can we use Loom?” — it’s “Where does Loom help most, where does reactive still win, and how do we combine them safely?” This session focuses on practical adoption patterns for virtual threads, structured concurrency, and reactive interoperability in real services.

Through two small demos, we’ll migrate an I/O-bound microservice to virtual threads, apply structured concurrency to improve cancellation and error handling, and explore hybrid designs that mix virtual-thread request handling with reactive pipelines for streaming and backpressure-sensitive workloads. We’ll also examine common production pitfalls such as thread-local leakage, blocking native calls, observability concerns, and misleading benchmarks.

Attendees will leave with concrete code patterns, a migration checklist, and a decision framework for choosing between virtual threads, reactive programming, and hybrid designs based on workload shape, ecosystem constraints, and operational goals.

---

### 2\) Talk title options

- Beyond Loom: Practical Patterns for Virtual Threads, Structured Concurrency, and Reactive Interop  
- Loom in Production: When Virtual Threads Win, When Reactive Still Matters  
- Virtual Threads Without Myths: Structured Concurrency and Reactive Interop in Java  
- Project Loom in Practice: Migration Patterns, Pitfalls, and Hybrid Architectures

My recommendation:  
**Beyond Loom: Practical Patterns for Virtual Threads, Structured Concurrency, and Reactive Interop**

---

### 3\) Talk synopsis for a recorded format

This recorded session shows how to adopt Java virtual threads pragmatically rather than ideologically. It explains where virtual threads simplify service code, where structured concurrency improves correctness, and why reactive programming still matters for streaming and backpressure-heavy workloads. Viewers will see two concise demos, practical migration guidance, benchmark framing, and a checklist they can apply to real systems.

---

### 4\) Target length for recording

For a recorded talk, I’d suggest:

- **30 minutes** if this is for general conference video content  
- **40–45 minutes** if this is a full technical session  
- **20–25 minutes** if you want a tight, high-retention online format

Best recommendation: **32–35 minutes recorded**

That gives enough room for:

- concept setup  
- 2 demos  
- trade-offs  
- checklist  
- crisp ending

---

### 5\) Recorded talk outline

## A. 32–35 minute structure

### 0–3 min — The real question after Loom

- Virtual threads are real and useful  
- But they do **not** replace every async/reactive design  
- Frame the talk around trade-offs, migration, and hybrid patterns

### 3–9 min — What virtual threads change

- Thread-per-request becomes viable again  
- Simpler imperative code  
- Easier debugging and stack traces  
- What changes operationally vs platform threads

### 9–14 min — Structured concurrency

- Treat related tasks as a unit  
- Cancellation propagation  
- Failure handling  
- Safer fan-out/fan-in  
- Why this matters more than “cheap threads”

### 14–21 min — Demo 1: Migrate a blocking I/O service

- Baseline endpoint  
- Switch executor/request handling to virtual threads  
- Show code diff  
- Discuss latency/throughput changes  
- Explain benchmark caveats

### 21–27 min — Reactive is not dead

- Where reactive still wins:  
  - backpressure  
  - streaming  
  - operator-rich pipelines  
  - extreme multiplexing  
- Compare programming models  
- Show interop options

### 27–31 min — Demo 2: Hybrid architecture

- Reactive gateway or stream pipeline  
- Virtual-thread workers for business logic / blocking integrations  
- Observability/context propagation  
- ThreadLocal caution

### 31–35 min — Adoption checklist and closing

- when to choose virtual threads  
- when to keep reactive  
- when to mix both  
- practical rollout checklist

---

### 6\) Slide deck skeleton

## Slide 1 — Title

**Beyond Loom: Practical Patterns for Virtual Threads, Structured Concurrency, and Reactive Interop**

Subtitle: How to adopt virtual threads pragmatically in production Java systems

---

## Slide 2 — Why this talk

- Loom changed Java concurrency  
- Simpler code is now possible  
- But migration decisions are still nuanced  
- The real goal: better systems, not trend adoption

---

## Slide 3 — The thesis

**Virtual threads simplify most I/O-bound service code.**  
**Reactive remains essential for backpressure-heavy streaming systems.**  
**The best production architectures are often hybrid.**

---

## Slide 4 — What Loom gives you

- Cheap threads  
- Blocking style with modern scalability  
- Better readability  
- More conventional debugging/profiling  
- Lower migration cost for blocking codebases

---

## Slide 5 — What Loom does not magically solve

- Blocking native calls  
- Poorly behaving libraries  
- ThreadLocal misuse  
- Unbounded concurrency  
- Bad benchmarks  
- Backpressure design

---

## Slide 6 — Structured concurrency in one idea

- A task should not outlive its parent scope  
- Child failures should be visible  
- Cancellation should be coordinated  
- Parallel work should be modeled explicitly

---

## Slide 7 — Structured concurrency benefits

- fewer leaks  
- clearer failure semantics  
- improved cancellation  
- easier reasoning  
- safer fan-out requests

---

## Slide 8 — Demo 1 setup

- simple I/O-bound service  
- downstream call \+ DB/mock I/O  
- baseline using conventional pool or blocking model  
- metrics to compare

---

## Slide 9 — Demo 1 code before/after

- baseline request handling  
- virtual thread executor / per-task model  
- note minimal code changes

---

## Slide 10 — Demo 1 results

- throughput  
- p95/p99 latency  
- memory profile  
- CPU utilization  
- caveat: results are workload dependent

---

## Slide 11 — Benchmark traps

- warmup issues  
- unrealistic mocks  
- hidden connection pool limits  
- downstream saturation  
- measuring throughput without tail latency

---

## Slide 12 — So does Loom obsolete reactive?

**No.**

- Different strengths  
- Different guarantees  
- Different ergonomics

---

## Slide 13 — Where reactive still wins

- explicit backpressure  
- continuous streams  
- high-volume transformations  
- event pipelines  
- bounded resource orchestration

---

## Slide 14 — Where virtual threads shine

- request/response services  
- blocking library integration  
- simpler application logic  
- developer productivity  
- easier migration

---

## Slide 15 — Hybrid pattern

- reactive edges for streams  
- virtual threads for request-scoped work  
- adapt at boundaries  
- pick the model that matches the workload

---

## Slide 16 — Demo 2 architecture

- reactive ingress/gateway  
- virtual-thread business workers  
- reactive stream to client or broker  
- observability across boundary

---

## Slide 17 — Pitfalls

- ThreadLocal leaks  
- pinning / blocking synchronized sections  
- native libraries  
- carrier-thread misconceptions  
- context propagation surprises

---

## Slide 18 — Observability

- tracing  
- MDC/logging context  
- thread naming  
- JFR and profiling  
- structured task visibility

---

## Slide 19 — Adoption checklist

- identify I/O-bound endpoints  
- verify library behavior  
- add benchmark gates  
- review ThreadLocal usage  
- validate tracing/logging  
- define rollback plan

---

## Slide 20 — Decision matrix

- choose virtual threads  
- choose reactive  
- choose hybrid  
- based on workload, team skill, ecosystem, ops needs

---

## Slide 21 — Closing

- Loom changed the default choice  
- reactive still matters  
- structured concurrency is the bigger design improvement  
- optimize for correctness, clarity, and measured outcomes

---

### 7\) Speaker script / presenter notes

## Opening (first minute)

“Project Loom changed the conversation in Java concurrency. For years, teams had to choose between code that was easy to read and code that scaled under I/O-heavy workloads. Virtual threads changed that trade-off dramatically. But they did not erase reactive programming, and they did not make concurrency design trivial. In this talk, I want to focus on practical patterns: where virtual threads help immediately, where structured concurrency improves correctness, and where reactive still remains the better tool.”

## Transition to structured concurrency

“If virtual threads are the headline feature, structured concurrency is arguably the deeper design improvement. Cheap threads are useful. But the real production benefit comes from making concurrent work easier to bound, cancel, and reason about.”

## Transition to demo 1

“Let’s move from theory to a simple service. This endpoint does what many real endpoints do: a couple of downstream I/O calls, some aggregation, and a response. The point of this demo is not that virtual threads are always faster. The point is how little code you need to change, and what you should measure afterward.”

## Transition to reactive section

“At this point, it’s tempting to claim that reactive is now unnecessary. That would be wrong. Reactive still solves problems that virtual threads do not solve as elegantly — especially explicit backpressure, rich stream composition, and long-lived high-volume pipelines.”

## Closing

“My recommendation is pragmatic: default to virtual threads for many service-style workloads, keep reactive where stream semantics and backpressure are central, and use hybrid boundaries where each model fits naturally. The goal is not ideological purity. The goal is simpler code, safer concurrency, and measurable operational outcomes.”

---

### 8\) Decision matrix slide content

| Scenario | Best fit |
| :---- | :---- |
| Typical request/response microservice with blocking I/O | Virtual threads |
| Existing blocking libraries, want simpler migration | Virtual threads |
| Kafka/file/network stream transformations with explicit backpressure | Reactive |
| Long-lived event streams to many consumers | Reactive |
| Gateway \+ business logic \+ some streaming edges | Hybrid |
| Team wants easiest debugging and lowest cognitive load | Virtual threads |
| Team already expert in Reactor and pipeline composition | Reactive or hybrid |

---

### 9\) Demo plan for recording

## Demo 1 — Blocking service to virtual threads

Use a small service that:

- receives a request  
- calls two downstream mock services  
- sleeps or performs simulated DB latency  
- aggregates results

Show:

- baseline with fixed thread pool  
- version using virtual threads  
- quick metrics summary

Keep the benchmark simple and honest:

- concurrency level  
- response time distribution  
- note that downstream bottlenecks dominate

## Demo 2 — Hybrid model

Use:

- reactive stream ingress or simulated Flux pipeline  
- offload blocking/business work appropriately  
- return or emit results downstream

Show:

- why backpressure logic is still natural in reactive  
- why business code is easier imperatively  
- where boundaries should live

---

### 10\) Suggested recorded-talk assets

Prepare:

- 20–22 slides  
- 2 runnable demos  
- 1 benchmark chart  
- 1 decision matrix  
- 1 final checklist slide

Useful extras:

- GitHub repo with:  
  - `/demo-virtual-threads`  
  - `/demo-hybrid-reactive`  
  - `/benchmarks`  
  - `/slides`  
- README with run commands  
- one-page adoption checklist PDF

---

### 11\) Short promotional blurb

Can Java virtual threads replace reactive programming? Not quite — but they do change the default choice for many services. This session explores how to use virtual threads, structured concurrency, and reactive interop together in production-style Java applications, with practical demos, migration advice, and performance trade-offs.

---

### 12\) Recording tips for this topic

For a recorded version:

- keep code snippets large and minimal  
- avoid too much benchmark detail on screen  
- use diagrams for thread/request flow  
- keep each demo under 5 minutes  
- summarize each section with one takeaway slide  
- explicitly state “measure your workload” to avoid overclaiming

---

### 13\) Strong closing line

**Virtual threads changed the default, not the laws of system design: use them to simplify service code, use reactive where flow control matters, and use structured concurrency to make parallel work safer by design.**

If you want, I can next turn this into either:

1. a **full speaker script**,  
2. a **slide-by-slide deck with bullets and visuals**, or  
3. a **demo repo structure with sample Java code snippets**.

# Script

Yes — below is a **30-minute, slide-by-slide speaker script** with **presenter instructions, timing, visuals, and demo cues**.

# 30-minute recorded talk script

**Title:** Beyond Loom: Practical Patterns for Virtual Threads, Structured Concurrency, and Reactive Interop

**Target length:** 30 minutes  
**Recommended slide count:** 16 slides  
**Format:** recorded technical talk with 2 short demos

---

## Overall pacing

- Intro \+ framing: 4 min  
- Loom \+ structured concurrency: 8 min  
- Demo 1: 5 min  
- Reactive vs Loom: 6 min  
- Demo 2: 4 min  
- Checklist \+ close: 3 min

---

# Slide 1 — Title

**Time:** 0:00–0:45

### Slide content

**Beyond Loom: Practical Patterns for Virtual Threads, Structured Concurrency, and Reactive Interop**

Subtitle: How to choose the right concurrency model in Java services

### Speaker script

“Hello, and welcome. In this talk, I want to go beyond the usual excitement around Project Loom and focus on practical engineering decisions. Virtual threads are a major step forward for Java, but the real value is not just that they are lightweight. The real value is that they change how we structure service code, how we reason about concurrent work, and how we integrate with existing reactive systems.

So this session is about three things: virtual threads, structured concurrency, and reactive interoperability. The goal is to help you decide when to use each, when to mix them, and what to watch out for in production.”

### Presenter instructions

- Keep energy calm and confident.  
- Don’t rush the title slide.  
- State early that this is a pragmatic, non-ideological talk.

### Visual suggestion

- Clean title slide with subtle diagram:  
  - Virtual Threads  
  - Structured Concurrency  
  - Reactive Interop

---

# Slide 2 — The big idea

**Time:** 0:45–1:45

### Slide content

**The thesis**

- Virtual threads simplify many I/O-bound services  
- Structured concurrency improves safety and clarity  
- Reactive is still the right tool for some workloads  
- Hybrid designs are often the best production answer

### Speaker script

“Here is the short version of the talk.

First, virtual threads simplify a very large class of Java services, especially request-response applications that spend a lot of time waiting on I/O.

Second, structured concurrency matters just as much as virtual threads, maybe more. It gives us a better model for cancellation, failure propagation, and scoped task lifecycles.

And third, reactive programming is not obsolete. It still remains extremely useful for explicit backpressure, continuous streams, and complex asynchronous data pipelines.

So the conclusion is not ‘replace everything with Loom.’ The conclusion is that the default choice has changed, and hybrid designs are often the most practical answer.”

### Presenter instructions

- This slide sets expectations.  
- Emphasize “default choice changed,” not “everything changed.”

### Visual suggestion

- 4-box layout, one point per box

---

# Slide 3 — Why this matters in 2026

**Time:** 1:45–3:00

### Slide content

**Why teams still struggle**

- Loom is available and production-ready  
- But migration paths are uneven  
- Blocking libraries, ThreadLocal usage, and observability still matter  
- Teams need patterns, not hype

### Speaker script

“By 2026, virtual threads are no longer a novelty. They are part of the real production conversation. The problem now is not awareness. The problem is adoption strategy.

Many teams have a lot of existing blocking code. Others are deeply invested in reactive systems. Some teams want simpler code, but they are unsure about performance, context propagation, logging, or library compatibility.

That is why practical patterns matter. Teams do not need more slogans. They need migration guidance, measurable trade-offs, and a reliable way to decide between imperative, reactive, and hybrid approaches.”

### Presenter instructions

- Speak to real-world uncertainty.  
- Make the audience feel understood.

### Visual suggestion

- Maturity curve:  
  - novelty  
  - experimentation  
  - adoption  
  - optimization

---

# Slide 4 — What virtual threads actually change

**Time:** 3:00–5:00

### Slide content

**Virtual threads make thread-per-task viable again**

- Synchronous-looking code with high concurrency  
- Lower cost than platform threads  
- Easier debugging and stack traces  
- Works well with many existing blocking APIs

### Speaker script

“The headline change with virtual threads is simple: thread-per-task becomes viable again for a huge number of workloads.

For a long time, Java developers had to choose between straightforward blocking code and scalable asynchronous models. With virtual threads, that trade-off changes. We can often keep imperative code, but still scale to large numbers of concurrent tasks.

That means code is easier to read. Control flow is easier to follow. Stack traces are more familiar. Existing blocking APIs can often be reused with much lower migration cost.

Now, that does not mean threads are suddenly free. It means they are cheap enough that the architectural default has shifted for many applications.”

### Presenter instructions

- Avoid going too deep into JVM internals.  
- Keep it operational and practical.

### Visual suggestion

- Before/after diagram:  
  - old: few platform threads \+ async callbacks  
  - new: many virtual threads \+ straightforward logic

---

# Slide 5 — What virtual threads do not solve

**Time:** 5:00–6:30

### Slide content

**Virtual threads are powerful, not magical**

- Blocking native or pinning-heavy code can still hurt  
- ThreadLocal misuse still leaks state  
- Unbounded concurrency is still dangerous  
- Backpressure is still a separate design concern

### Speaker script

“Now let’s be careful. Virtual threads solve some problems very well, but they do not repeal the laws of systems design.

If you are calling blocking native libraries or code paths that pin underlying carrier threads too often, you can still lose scalability.

If your application has sloppy ThreadLocal hygiene, virtual threads do not magically fix that. In fact, misuse may spread more widely if concurrency increases.

If you create unbounded concurrent work, cheap threads will simply let you fail at a larger scale.

And importantly, virtual threads do not replace backpressure as a concept. They simplify request concurrency, but they do not automatically give you the semantics of bounded stream processing.”

### Presenter instructions

- Use this slide to establish credibility.  
- Important phrase: “powerful, not magical.”

### Visual suggestion

- Warning icons next to each pitfall

---

# Slide 6 — Structured concurrency in one sentence

**Time:** 6:30–8:00

### Slide content

**Structured concurrency** “Concurrent tasks should have a clear parent scope, shared lifetime, and coordinated cancellation/failure handling.”

### Speaker script

“If virtual threads are what gets attention, structured concurrency is what makes concurrent programs safer.

Here is the key idea: if you split work into concurrent subtasks, those tasks should belong to a parent scope. They should begin within that scope, finish within that scope, and if one fails or the parent is cancelled, the others should be handled consistently.

This gives us something that ordinary ad hoc futures often do not: clear ownership, better failure behavior, and fewer resource leaks.

When teams adopt Loom, I strongly encourage them not just to swap executors, but to rethink concurrency structure at the same time.”

### Presenter instructions

- Slow down slightly here.  
- This is a conceptual slide.

### Visual suggestion

- Parent task with 3 child tasks in a bounded box  
- failure arrow/cancellation arrow

---

# Slide 7 — Why structured concurrency helps

**Time:** 8:00–9:30

### Slide content

**Practical benefits**

- Better cancellation propagation  
- Clearer failure semantics  
- Fewer orphaned tasks  
- Easier fan-out / fan-in logic  
- Code is easier to reason about under failure

### Speaker script

“In practice, structured concurrency helps in places where service code usually gets messy.

Imagine a request handler that calls three downstream services in parallel. With unstructured futures, you have to remember what happens if one fails, whether others are cancelled, whether timeouts are coordinated, and whether partially completed work leaks resources.

Structured concurrency gives you a cleaner model. Child tasks belong to the request scope. If one fails, you can fail fast. If the request times out, the children are cancelled. That is a real correctness improvement, not just a style preference.”

### Presenter instructions

- Use a relatable example: fan-out to 3 downstream services.

### Visual suggestion

- Table:  
  - Unstructured: manual cleanup, hidden failure paths  
  - Structured: scoped lifecycle, coordinated cancellation

---

# Slide 8 — Demo 1 setup

**Time:** 9:30–10:30

### Slide content

**Demo 1: migrating an I/O-bound endpoint**

- Endpoint calls two downstream services  
- Simulated latency: network \+ DB  
- Baseline: traditional fixed pool / blocking model  
- Variant: virtual threads  
- Measure throughput, latency, memory

### Speaker script

“Let’s make this concrete with a small demo.

This service handles a request, calls two downstream dependencies, waits on I/O, aggregates the response, and returns. It is a very normal microservice shape.

I’ll show a baseline version using a traditional executor model, then a virtual-thread version. The goal is not to prove that virtual threads always win. The goal is to show how small the code change can be, and how to think about measurement afterward.”

### Presenter instructions

- Set expectations: this is illustrative, not universal proof.  
- Transition into live code or screenshots.

### Visual suggestion

- Architecture diagram: client → service → service A \+ service B \+ DB/mock

---

# Slide 9 — Demo 1 code and results

**Time:** 10:30–14:30

### Slide content

**What changed**

- request handling moved to virtual-thread-per-task execution  
- business logic stayed imperative  
- downstream calls stayed blocking  
- concurrency increased without callback refactoring

**What to measure**

- p95/p99 latency  
- throughput  
- heap/memory profile  
- downstream bottlenecks

### Speaker script

“In the baseline version, requests are handled with a conventional bounded executor. Under load, that means requests queue behind a relatively small number of threads while those threads spend a lot of time waiting on I/O.

In the virtual-thread version, each task gets its own lightweight thread. The business logic stays almost identical. We do not rewrite the code into chained callbacks or reactive operators. We keep the imperative flow.

What usually happens in this kind of workload is that code simplicity improves immediately, and concurrency handling becomes much less awkward. In many I/O-bound cases, throughput improves and latency remains competitive or improves as well.

But this is where caution matters. The real bottleneck might be the database pool, the HTTP client, or a downstream service. If those are the true limiters, virtual threads won’t magically remove them. So the right conclusion from a demo is never ‘Loom is always faster.’ The right conclusion is ‘measure your full system under realistic limits.’”

### Presenter instructions

- If showing code, keep it brief and legible.  
- Spend more time on interpretation than on syntax.

### Visual suggestion

- Side-by-side tiny code diff  
- Simple benchmark chart with baseline vs virtual threads  
- One footer note: “Workload dependent”

### Demo instruction

If recording with live demo:

- Show only:  
  - baseline endpoint  
  - executor change  
  - sample metrics  
- Keep this under 4 minutes on screen

---

# Slide 10 — Benchmark traps

**Time:** 14:30–16:00

### Slide content

**Common benchmarking mistakes**

- measuring toy workloads with no real contention  
- ignoring warmup/JIT effects  
- forgetting connection pool limits  
- reporting throughput without tail latency  
- not measuring failure or timeout behavior

### Speaker script

“Because Loom discussions often become benchmark discussions, I want to pause here for one minute on benchmark traps.

The easiest mistake is benchmarking a toy workload that is too clean. Another is forgetting warmup and JIT behavior. Another very common problem is ignoring pool limits. If your DB pool has 20 connections, creating 20,000 virtual threads does not create 20,000 useful DB operations.

And you should not report throughput alone. Tail latency matters. Timeout behavior matters. Cancellation behavior matters. In real production systems, failure behavior is often more important than peak benchmark numbers.”

### Presenter instructions

- Deliver this as practical advice, not criticism.  
- Good credibility builder.

### Visual suggestion

- Checklist graphic with red X icons

---

# Slide 11 — So, is reactive obsolete?

**Time:** 16:00–17:00

### Slide content

**No.** Virtual threads changed the default for many services.  
Reactive still wins for some classes of problems.

### Speaker script

“At this point, we can ask the question directly: did Loom obsolete reactive programming in Java?

No. It did not.

What Loom did was change the default for many service-style applications. It made imperative concurrency much more attractive again. But reactive programming still offers capabilities that remain extremely valuable in the right domains.”

### Presenter instructions

- Short, crisp, direct.  
- Good reset point halfway through talk.

### Visual suggestion

- Large “No.” with two columns beneath

---

# Slide 12 — Where reactive still wins

**Time:** 17:00–19:30

### Slide content

**Reactive remains strong for**

- explicit backpressure  
- streaming pipelines  
- long-lived event flows  
- rich async composition operators  
- bounded resource control in dataflow-heavy systems

### Speaker script

“Reactive programming is still the better fit when the problem is fundamentally about streams and flow control.

If you are processing events continuously, applying transformations, batching, windowing, combining streams, or managing producer-consumer pressure explicitly, reactive abstractions are very powerful.

Reactive Streams gives you backpressure semantics as part of the model. Frameworks like Reactor provide rich operator composition that is natural for data pipelines.

So if your system is stream-oriented rather than request-oriented, or if explicit demand management is central to correctness, reactive is still often the better tool.”

### Presenter instructions

- Acknowledge reactive strengths clearly.  
- This helps avoid sounding ideological.

### Visual suggestion

- Examples:  
  - Kafka pipeline  
  - WebSocket/streaming  
  - event transformation graph

---

# Slide 13 — Where virtual threads shine

**Time:** 19:30–21:00

### Slide content

**Virtual threads are a strong default for**

- request/response services  
- blocking integrations  
- straightforward business workflows  
- teams optimizing for readability/debuggability  
- lower-friction migration from existing code

### Speaker script

“Virtual threads are especially attractive for classic service workloads.

If you have request-response APIs, code that calls databases and HTTP services, logic that is easier to express step by step, and a team that values readability and straightforward debugging, virtual threads are often an excellent fit.

They also dramatically lower migration cost. You can often keep the structure of existing code and improve scalability characteristics without redesigning the whole application around async composition.”

### Presenter instructions

- Make this feel practical and empowering.  
- Tie back to everyday service development.

### Visual suggestion

- Examples:  
  - REST API  
  - internal microservice  
  - blocking HTTP/DB integration

---

# Slide 14 — Hybrid architecture is often best

**Time:** 21:00–22:30

### Slide content

**Hybrid pattern**

- reactive at streaming boundaries  
- virtual threads for request-scoped business logic  
- adapt at clear boundaries  
- choose by workload shape, not ideology

### Speaker script

“In many real systems, the best answer is not one model everywhere. It is a hybrid.

You might use reactive components at the boundaries where stream semantics and backpressure really matter: gateways, messaging pipelines, continuous event processing.

Inside the service, where business logic is request-scoped and easier to express imperatively, you can use virtual threads.

This gives you the best of both worlds: strong flow control where it matters, and simpler code where that is more valuable.”

### Presenter instructions

- This is the bridge to Demo 2\.  
- Emphasize boundaries and separation of concerns.

### Visual suggestion

- Diagram: reactive ingress → adapter boundary → virtual-thread workers → response/stream

---

# Slide 15 — Demo 2: hybrid interop

**Time:** 22:30–26:00

### Slide content

**Demo 2**

- reactive edge handles stream/backpressure  
- business logic runs in simpler imperative style  
- watch context propagation and ThreadLocal assumptions  
- instrument both sides clearly

### Speaker script

“For the second demo, the point is not raw performance. The point is architecture.

At the edge, we keep a reactive model because the system is handling a stream and wants explicit control over flow. But once an item reaches the business processing stage, we use a simpler imperative style that maps naturally to virtual-thread execution.

This can be a very effective boundary, but there are caveats. You need to be careful about context propagation for logging and tracing. You should not assume ThreadLocal state appears automatically where you want it. And observability needs to cover both models cleanly.

So the lesson from the hybrid approach is this: interop is viable, but boundaries should be deliberate and observable.”

### Presenter instructions

- Keep this demo architectural.  
- Don’t drown in framework specifics unless your audience is framework-specific.

### Visual suggestion

- 3-stage pipeline diagram with one stage highlighted as reactive, one as virtual-thread

### Demo instruction

If live:

- Show one reactive source  
- Show adapter into imperative handler  
- Show log/tracing note  
- Keep under 3.5 minutes

---

# Slide 16 — Pitfalls, checklist, and closing

**Time:** 26:00–30:00

### Slide content

**Adoption checklist**

- Start with I/O-bound endpoints  
- Audit ThreadLocal and context propagation  
- Validate blocking/native library behavior  
- Benchmark with real bottlenecks and tail latency  
- Add observability before rollout  
- Keep reactive where backpressure is core  
- Prefer hybrid designs when boundaries are clear

**Final takeaway** Virtual threads changed the default.  
Reactive still matters.  
Structured concurrency makes concurrent code safer.

### Speaker script

“Let me close with a practical checklist.

Start with I/O-bound endpoints where the value is usually clearest. Audit your use of ThreadLocal state and your tracing or logging context propagation. Validate any native or specialized blocking libraries. Benchmark real workloads, not toy ones, and include tail latency and failure behavior. Make observability part of the rollout, not an afterthought.

And importantly, keep reactive where backpressure and streaming semantics are central. Use hybrid designs when boundaries are clear and beneficial.

So the final takeaway is this: virtual threads changed the default, but they did not make architectural thinking unnecessary. Reactive still matters. And structured concurrency is one of the most important tools for making concurrent Java code safer and easier to reason about.

Thank you.”

### Presenter instructions

- Slow down and end cleanly.  
- This is your strongest slide for retention.  
- Pause after the final sentence.

### Visual suggestion

- Checklist on left  
- 3-line takeaway on right

---

# Recording notes

## Delivery style

- Aim for **conversational technical clarity**, not hype.  
- Speak at a moderate pace: about **120–135 words per minute**.  
- Leave small pauses after key lines:  
  - “Virtual threads are powerful, not magical.”  
  - “Reactive is not obsolete.”  
  - “The default changed.”  
  - “Choose by workload shape, not ideology.”

## Demo handling

- Pre-record demos if possible.  
- Avoid terminal noise and long startup logs.  
- Zoom in on code.  
- Use highlighted diffs instead of scrolling through whole files.

## Visual rhythm

Try to alternate:

- concept slide  
- comparison slide  
- diagram  
- code/result slide  
- checklist

That helps a 30-minute recorded talk feel dynamic.

---

# Optional slide appendix if you want backups

Use only if needed; not part of main 30 minutes:

- Appendix A: platform threads vs virtual threads  
- Appendix B: pinning and synchronized/native caveats  
- Appendix C: sample decision matrix  
- Appendix D: benchmark methodology notes

---

# One-line closing for the recording

**Use virtual threads to simplify service code, use reactive where flow control is fundamental, and use structured concurrency to make parallel work safer by design.**

If you want, I can next produce either:

1. a **speaker-ready full transcript** of the 30-minute talk,  
2. a **PowerPoint/Google Slides slide text pack**, or  
3. **demo code snippets for Slide 9 and Slide 15**.

