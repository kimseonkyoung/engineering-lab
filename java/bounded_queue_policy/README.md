# 대기열이 차면 누가 대가를 치르나 — 스레드 풀의 거부 정책 네 가지

줄기: A4 백프레셔 · E3 실행기 / 축: ② (사내 비동기 처리·연결 풀) ③ (관문 프록시의 과부하 처리)

- 날짜 / Date: 2026-11-10
- 출처 / Source: `java.util.concurrent.ThreadPoolExecutor` 문서 · 선행 실험 [`cs/semaphore_throttle`](../../cs/semaphore_throttle/)

## Environment

| 항목 | 값 |
|---|---|
| JDK | 1.8.0_292 (ojdkbuild) |
| OS | Windows 10 (10.0) |
| CPU 코어 수 / cores | 4 (x86-64) |
| 실행 장소 / Where | 회사 (폐쇄망), Eclipse |
| JVM 인자 | Eclipse 기본값만 (`-Dfile.encoding=UTF-8`) |
| 기타 / Other | 실행마다 Program arguments만 바꿔 각 1회. 회사 코드는 `package javalab.bounded_queue_policy;`가 붙어 있고, 저장소 코드는 패키지 줄을 뺐다 |

## Question

1. 일꾼 2명 + 대기열 3칸인 스레드 풀에 처리 속도보다 빠르게 일을 계속 넣으면, 대기열이 찬 뒤에 오는 일은 어떻게 되나?
2. 거부 정책 네 가지(`Abort` · `CallerRuns` · `Discard` · `DiscardOldest`)는 각각 **누구에게** 대가를 치르게 하나 — 넣는 쪽, 새 일, 오래 기다린 일?
3. 대기열에 크기 제한을 없애면 무엇이 좋아지고 무엇이 나빠지나?

## Hypothesis

일은 10ms마다 하나씩 20개, 일 하나는 100ms. 처리 속도는 초당 20개(일꾼 2 × 100ms), 들어오는 속도는 초당 100개 — 5배 과부하. 풀의 수용량은 일꾼 2 + 대기열 3 = 5.

| 실행 | done | 버린 수 | 넣는 쪽 시간 | 살아남은 번호 |
|---|---|---|---|---|
| `abort` | 7 | 13 (예외로 앎) | ≈200ms | 0~4, 10, 11 |
| `discard` | 7 | 13 (모름) | ≈200ms | 0~4, 10, 11 |
| `oldest` | 7 | 13 (모름) | ≈200ms | 0, 1, 7, 8, 17, 18, 19 |
| `caller` | 20 | 0 | ≈700ms | 전부 |
| `abort 0` (대기열 무한) | 20 | 0 | ≈200ms | 전부, 최대 지연 ≈800ms |

- `abort`와 `discard`는 숫자가 같고, 넣는 쪽이 버림을 **알았느냐**만 다르다.
- `oldest`는 같은 개수를 살리지만 **최근 일**이 산다. 그래서 지연이 가장 짧다.
- `caller`는 하나도 안 버리는 대신 **넣는 쪽이 늦어진다** — 압력이 넣는 쪽으로 거꾸로 전달된다.
- 대기열이 무한이면 거부 정책은 한 번도 불리지 않고, 대가는 지연으로 옮겨 간다.

## Experiment

`BoundedQueuePolicy.java` 하나. `ThreadPoolExecutor(2, 2, …, 대기열, 거부 정책)`를 만들고, main 스레드가 `intervalMs`마다 `execute()`로 일을 넣는다. 일 하나는 `Thread.sleep(workMs)`.

실행 인자 `[mode] [queueCap] [tasks] [intervalMs] [workMs]`, 기본값 `abort 3 20 10 100`. `queueCap`이 0 이하면 크기 제한 없는 `LinkedBlockingQueue`. 모드 오타는 예외.

세는 값: `done`(끝난 일), `thrown`(넣는 쪽이 받은 예외 — `abort`만), `silentlyLost`(넣은 수 − 끝난 수 − 예외 수, 신호 없이 사라진 일), `ranByCaller`(main이 직접 한 일), `maxQueue`, `submit`(넣는 쪽이 20개를 다 넣기까지), `total`, 지연(넣은 순간 ~ 끝난 순간)의 평균·최대, 살아남은 번호.

### 기본 실행
| 순서 | 인자 |
|---|---|
| 1 | `abort` |
| 2 | `discard` |
| 3 | `oldest` |
| 4 | `caller` |

### 심화 실행
| 순서 | 인자 | 무엇을 밀었나 |
|---|---|---|
| 5 | `abort 0` | 대기열 크기 제한을 없앰 |
| 6 | `caller 3 20 10 300` | 일을 3배 무겁게 — 넣는 쪽이 직접 하는 일도 3배 |

### 실무 연결
`Executors.newFixedThreadPool(n)`과 스프링 `ThreadPoolTaskExecutor`(기본 `queueCapacity = Integer.MAX_VALUE`)는 둘 다 기본이 **크기 제한 없는 대기열**이다. 즉 5번 실행과 같아서 거부 정책은 불리지 않고, 과부하는 지연과 메모리로 간다. 거부 정책은 대기열에 상한을 준 뒤에야 의미가 생긴다. `ThreadPoolExecutor`에는 "기다리기" 정책이 없는데(`execute()`는 대기열에 `offer()`를 쓰고 `put()`을 쓰지 않음), 넣는 쪽을 멈춰 세우면 넣는 쪽이 웹 요청 스레드일 때 서버 전체가 멈출 수 있어서다. `CallerRunsPolicy`가 그 자리를 "재우는 대신 일을 시켜 늦추는" 방식으로 메운다.

## Result

원본 콘솔은 회사에서 화면 사진으로 남겼다.

```
[abort  ] done=9  thrown=11 silentlyLost=0  ranByCaller=0 maxQueue=3  submit=369ms  total=594ms  avgLatency=209ms maxLatency=285ms
          doneIds=[0, 1, 2, 3, 4, 8, 9, 15, 16]
[discard] done=9  thrown=0  silentlyLost=11 ranByCaller=0 maxQueue=3  submit=376ms  total=603ms  avgLatency=210ms maxLatency=284ms
          doneIds=[0, 1, 2, 3, 4, 8, 9, 15, 16]
[oldest ] done=9  thrown=0  silentlyLost=11 ranByCaller=0 maxQueue=3  submit=374ms  total=600ms  avgLatency=155ms maxLatency=240ms
          doneIds=[0, 1, 5, 6, 12, 13, 17, 18, 19]
[caller ] done=20 thrown=0  silentlyLost=0  ranByCaller=4 maxQueue=3  submit=784ms  total=921ms  avgLatency=174ms maxLatency=287ms
          doneIds=[0..19]
[abort  ] (queueCap=unbounded)
          done=20 thrown=0  silentlyLost=0  ranByCaller=0 maxQueue=14 submit=367ms  total=1137ms avgLatency=446ms maxLatency=783ms
          doneIds=[0..19]
[caller ] (workMs=300)
          done=20 thrown=0  silentlyLost=0  ranByCaller=5 maxQueue=3  submit=1924ms total=2555ms avgLatency=505ms maxLatency=844ms
          doneIds=[0..19]
```

### 요약

| 실행 | done | 버림 | 버림을 아나 | 넣는 쪽 시간 | 평균 / 최대 지연 | 살아남은 번호 | 가설 |
|---|---|---|---|---|---|---|---|
| `abort` | 9 | 11 | 예 (예외) | 369ms | 209 / 285ms | 0~4, 8, 9, 15, 16 | 개수 다름 |
| `discard` | 9 | 11 | **아니오** | 376ms | 210 / 284ms | 0~4, 8, 9, 15, 16 | 개수 다름 |
| `oldest` | 9 | 11 | 아니오 | 374ms | **155 / 240ms** | 0, 1, 5, 6, 12, 13, 17, 18, 19 | 개수 다름, 경향 일치 |
| `caller` | **20** | 0 | — | **784ms** | 174 / 287ms | 전부 | 일치 |
| 대기열 무한 | 20 | 0 | — | 367ms | **446 / 783ms** | 전부 | 일치 |
| `caller` 일 300ms | 20 | 0 | — | **1,924ms** | 505 / 844ms | 전부 | 일치 |

## What I learned

### 1. 가설과 다른 곳: 7개가 아니라 9개가 살았다 — 10ms가 10ms가 아니었다
가설은 "10ms마다 하나"를 전제로 했는데, `submit`이 200ms가 아니라 약 370ms였다. Windows 기본 타이머 해상도가 약 15.6ms라 `Thread.sleep(10)`이 한 틱(15.6ms)으로, `sleep(100)`이 일곱 틱(109.4ms)으로 늘어났다(1번 실험의 `sleep(200)` → 215ms와 같은 원인).

도착 간격 15.6ms · 작업 109.4ms로 같은 규칙을 다시 계산하면, `abort`는 `[0, 1, 2, 3, 4, 8, 9, 15, 16]`, `oldest`는 `[0, 1, 5, 6, 12, 13, 17, 18, 19]`가 나와 **실측과 번호까지 정확히 일치**한다. 규칙은 맞았고, 입력이 코드에 적힌 것과 달랐다. 도착 속도가 초당 100개가 아니라 약 64개(과부하 5배가 아니라 약 3.4배)가 되어 빈자리를 더 자주 만났다.

교훈: **실험의 입력 속도도 측정해야 한다.** 코드에 쓴 숫자가 실제로 일어난 일이라고 가정하면 안 된다. `submit`을 따로 찍어 둔 덕에 원인을 찾았다.

### 2. `abort`와 `discard`는 결과가 같고, 아는지 모르는지만 다르다
완료 9, 버림 11, 살아남은 번호까지 똑같다. 차이는 `thrown` 11과 `silentlyLost` 11 — 넣는 쪽이 예외로 **알았느냐**, 아무 신호 없이 **몰랐느냐**뿐이다. `discard`는 넣은 수에서 끝난 수를 빼는 계산을 따로 하지 않으면 몇 개가 사라졌는지 알 길이 없다. 실무에서 `DiscardPolicy`가 위험한 이유다.

### 3. `oldest`는 같은 개수를 살리지만 "최근 일"을 살린다
살아남은 번호가 `abort`(앞쪽 0~4, 그다음 짝)와 정반대로, 대기열 맨 앞의 오래된 일을 버리고 새 일을 넣으니 **매 순간 가장 최근에 온 일**이 산다(17, 18, 19). 그래서 평균 지연 155ms, 최대 240ms로 가장 짧다. 위치 갱신·시세처럼 **최신 값만 의미 있는 일**에 맞는 정책이다.

번호가 둘씩 짝(8·9, 15·16 / 5·6, 12·13)으로 사는 건 일꾼 둘이 거의 같은 틱에 일을 끝내 대기열에서 두 칸을 한꺼번에 비우기 때문이다.

### 4. `caller`는 하나도 안 버리는 대신 넣는 쪽을 늦춘다 — 백프레셔의 원래 뜻
대기열이 찼을 때 main이 일을 직접 4번 했고, 그동안 다음 일을 못 넣으니 `submit`이 369ms → 784ms로 약 2배가 됐다. 일을 300ms로 무겁게 하자 1,924ms, 약 5배. 넣는 속도가 처리 속도에 맞춰 **강제로** 느려진 것이고, 압력이 넣는 쪽으로 거꾸로 전달된다는 백프레셔의 말뜻 그대로다. 대가는 넣는 쪽 스레드가 그 시간 동안 **다른 일을 못 한다**는 것이다(넣는 쪽이 웹 요청 스레드라면 다른 요청도 못 받는다).

### 5. 대기열이 무한이면 거부는 없고, 대가는 지연으로 숨는다
하나도 안 버렸지만 대기열이 14까지 쌓였고, 마지막 일은 넣은 지 783ms 뒤에 끝났다(대기열 3칸일 때 최대 285ms의 약 2.7배). 일이 20개에서 끝났기에 이 정도지, 계속 들어오면 대기열과 지연이 끝없이 자란다. 이때 거부 정책은 한 번도 불리지 않는다. 기본값이 무한 대기열인 `newFixedThreadPool`, 스프링 `ThreadPoolTaskExecutor`에서 거부 정책을 아무리 골라도 소용없는 이유다.

### 6. 1번과 합치면: 넘치는 요청의 운명은 네 가지
| 손잡이 | 세마포어 (1번) | 스레드 풀 (2번) | 대가를 치르는 쪽 |
|---|---|---|---|
| 기다리기 | `acquire()` | 없음 (`execute()`는 `put` 대신 `offer`) — 대기열 무한이 사실상 이것 | 지연 |
| 거절 (알림) | `tryAcquire()` → `false` | `AbortPolicy` → 예외 | 새 일 (넣는 쪽이 앎) |
| 거절 (조용히) | — | `DiscardPolicy` | 새 일 (아무도 모름) |
| 오래된 것 버리기 | — | `DiscardOldestPolicy` | 오래 기다린 일 |
| 시한부 | `tryAcquire(t)` | (풀에는 없음) | 기한 넘긴 일 |
| **넣는 쪽 늦추기** | — | `CallerRunsPolicy` | **넣는 쪽** |

### 한 줄 결론
**거부 정책은 과부하의 대가를 누구에게 지울지 — 넣는 쪽(예외 또는 직접 일하기), 새 일, 오래된 일 — 를 고르는 것이고, 대기열이 무한이면 정책은 불리지 않고 대가가 지연으로 숨는다.**

### 내가 느낀 점
거부 정책은 과부하의 대가를 누구에게 지울지 고르는 것이고, 어떤 정책이 맞는지는 비즈니스 요구가 정한다. 그래서 정책은 로직 안에 박아 두지 말고 바깥에서 바꿔 끼울 수 있게 떼어 놓아야 한다. `ThreadPoolExecutor`가 거부 정책을 인터페이스로 받는 것이 그 구조다. 다만 대기열이 무한이면 정책이 한 번도 불리지 않았듯, 로직이 정책의 자리를 열어 둬야 정책이 의미가 있다.

### 이 실험의 한계
- 각 정책을 1회씩만 돌렸다. 틱 정렬에 따라 번호가 한두 개 달라질 수 있다.
- 작업이 `sleep`이라 CPU를 쓰지 않는다. `caller`에서 main이 일을 하는 동안 실제 CPU 경쟁은 없다.
- 일꾼 수를 핵심 = 최대 = 2로 고정했다. 핵심 수와 최대 수가 다를 때 어떤 순서로 차는지는 다음 실험의 몫이다.
- `oldest`의 짝 패턴, `caller`의 `ranByCaller` 4(가설 5)는 틱 정렬로 설명되지만 이번 실행만으로 확정하진 않았다.

## Next question

- [ ] 핵심 수 2, 최대 수 4, 대기열 3이면 세 번째 일꾼은 언제 생기나? 대기열이 무한이면 영원히 안 생기나? → `java/threadpool_params`
- [ ] 도착 간격을 `nanoTime` 기준으로 맞추거나(틱에 안 묶이게) 간격을 32ms로 두면 가설 산수와 정확히 맞아떨어지나?
- [ ] `CallerRunsPolicy`에서 넣는 쪽을 웹 요청 스레드라고 보면, 요청 스레드 수 대비 얼마나 많은 요청이 지연되나?
- [ ] 직접 만든 거부 처리기에서 `queue.put()`을 부르면 "기다리기" 정책이 되나? `shutdown()`과 함께 쓰면 어떤 문제가 생기나?
