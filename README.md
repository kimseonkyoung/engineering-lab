# engineering-lab

Java, Spring, Netty, Database, CS, AI Systems를 공부하면서 작은 실험과 관찰 결과를 누적하는 개인 학습용 레포입니다.
A personal learning repo for accumulating small experiments and observations while studying Java, Spring, Netty, databases, CS, and AI systems.

완성된 프로젝트가 아니라 **작은 실험과 검증 기록을 쌓는 공간**입니다.
This is not a finished project — it is **a place to collect small experiments and verification notes**.

## 디렉터리 / Directories

| 디렉터리 | 역할 | Purpose |
|---|---|---|
| `java/` | Java 언어, JVM, 동시성 API의 실제 동작 | Java language, JVM, concurrency APIs in practice |
| `spring/` | Spring / Spring Boot 동작 방식 | How Spring / Spring Boot works |
| `netty/` | Netty, 네트워크 I/O, 이벤트 루프 | Netty, network I/O, event loops |
| `database/` | SQL, 인덱스, 트랜잭션 | SQL, indexes, transactions |
| `ai/` | LLM, 에이전트, 추론/서빙 | LLMs, agents, inference/serving |
| `cs/` | 운영체제, 네트워크, 자료구조/알고리즘 개념 | OS, networking, data structures/algorithms concepts |

### 경계 규칙 / Boundary rule

- **개념**을 확인하는 실험은 `cs/`, **자바 API나 JVM의 실제 동작**을 확인하는 실험은 `java/`에 둡니다.
  Experiments that verify a *concept* go in `cs/`; experiments that verify *how a Java API or the JVM actually behaves* go in `java/`.
  - 예: 세마포어로 동시 실행 수 제한 → `cs/` / `Semaphore`의 fair 옵션이 처리량에 주는 영향 → `java/`
- 네트워크도 같습니다: TCP 개념은 `cs/`, Netty 동작은 `netty/`.
  Same for networking: TCP concepts in `cs/`, Netty behavior in `netty/`.

## 실험 단위 / Experiment unit

실험 하나 = 폴더 하나. 폴더 이름은 날짜가 아니라 **주제**로 짓습니다.
One experiment = one folder, named by **topic**, not by date.

```
cs/
  semaphore-throttle/
    README.md                 # TEMPLATE.md 양식으로 작성
    SemaphoreThrottle.java
```

- 순수 Java 실험은 빌드 도구 없이 단일 파일로 실행합니다: `java SemaphoreThrottle.java` (JDK 11+)
  Plain Java experiments run as single source files without a build tool.
- 의존성이 필요한 실험(Spring 등)만 해당 폴더 안에 별도 빌드 설정을 둡니다.
  Only experiments that need dependencies (e.g. Spring) carry their own build config.
- **예외:** 회사 실험은 JDK 8 + Eclipse로 실행하므로 단일 파일 실행(JDK 11+) 규칙의 예외로 둡니다. 이때 README의 Environment에 실행 방법을 적습니다.
  **Exception:** Experiments at work run on JDK 8 + Eclipse, so they are exempt from the single-file rule; record how they were run in the Environment section.

## 학습 기록 원칙 / How to Record

각 실험은 [`TEMPLATE.md`](TEMPLATE.md) 양식을 복사해 기록합니다. / Copy [`TEMPLATE.md`](TEMPLATE.md) for each experiment.

1. **Environment** — 어떤 환경에서 돌렸는가? / Where and on what did I run it?
2. **Question** — 무엇이 궁금한가? / What do I want to know?
3. **Hypothesis** — 어떻게 동작할 것이라 예상하는가? / What do I expect to happen?
4. **Experiment** — 어떻게 확인했는가? / How did I test it?
5. **Result** — 실제로 무엇이 관찰되었는가? / What did I actually observe?
6. **What I learned** — 예상과 무엇이 같고 달랐는가? / What matched or differed from my expectation?
7. **Next question** — 다음에 확인할 것은? / What should I check next?

다음에 확인할 질문 후보는 [`BACKLOG.md`](BACKLOG.md)에 모읍니다. / Candidate questions live in [`BACKLOG.md`](BACKLOG.md).

## 원칙 / Rules

- 실험은 작고 독립적으로 유지합니다. Keep each experiment small and self-contained.
- 환경이 다르면 결과도 다를 수 있으므로 환경을 반드시 기록합니다. Always record the environment — results depend on it.
- GitSieve 같은 실제 사이드 프로젝트는 이 레포에 넣지 않고 별도 저장소로 관리합니다. Real side projects such as GitSieve live in their own repositories, not here.

## 실험 목록 / Experiment Log

| 날짜 / Date | 디렉터리 / Dir | 실험 / Experiment | 한 줄 결론 / One-line takeaway |
|---|---|---|---|
| 2026-10-08 | `cs/` | [race-condition](cs/race-condition/) | 락 없는 증가는 갱신을 잃고, JIT는 그 버그를 숨길 뿐이며, `volatile`은 원자성을 주지 않는다 — 락만 매번 정답 |
