# 세마포어로 동시 실행 수를 제한하면, 넘치는 요청은 어디로 가나

줄기: A2 동기화 · A4 백프레셔 / 축: ①③ (연세 구술) ② (배차 검사 연결 풀 대기 시간)

- 날짜 / Date: 2026-11-05
- 출처 / Source: OSTEP 31.1~31.3 (세마포어 정의, 락·조건 변수로서의 세마포어), 31.7 (쓰레드 제어) · 선행 실험 [`cs/lock_implementations`](../lock_implementations/)

## Environment

| 항목 | 값 |
|---|---|
| JDK | 1.8.0_292 (ojdkbuild) |
| OS | Windows 10 (10.0) |
| CPU 코어 수 / cores | 4 (x86-64) |
| 실행 장소 / Where | 회사 (폐쇄망), Eclipse |
| JVM 인자 | Eclipse 기본값만 (`-Dfile.encoding=UTF-8`). `acquire` 2회차 한 번은 커버리지 실행이라 `-javaagent:…jacocoagent.jar`가 붙었다 |
| 기타 / Other | 실험 당일 코드는 1회 실행판이라 실행마다 인자를 바꿔 넣었다. 저장소 코드는 한 번 실행에 3회 반복하도록 고쳤다 |

## Question

1. `new Semaphore(3)`은 정말 동시에 3개만 일하게 하나? (`maxRunning`이 자리 수를 넘지 않나)
2. 자리가 없을 때 넘치는 요청을 **기다리게(acquire) / 거절(tryAcquire) / 잠깐만 기다리게(tryAcquire(timeout))** 하면 완료 수·거절 수·대기 줄·총 시간이 어떻게 갈리나?
3. 처리량(초당 완료 수)이 높은 쪽이 정말 "더 좋은" 쪽인가?

## Hypothesis

작업 200ms, 스레드 10개, 자리 3개면 3·3·3·1 네 묶음이다. 여기서 산수로 나온다.

| 실행 | done | rejected | maxRunning | maxWaiting | elapsed | throughput |
|---|---|---|---|---|---|---|
| acquire | 10 | 0 | 3 | 7 | ≈800ms | ≈12.5/s |
| try | 3 | 7 | 3 | 0 | ≈200ms | ≈15/s |
| timed (대기 300ms) | 6 | 4 | 3 | 7 | ≈400ms | ≈15/s |
| timed (대기 100ms) | 3 | 7 | 3 | 7 | ≈200ms | ≈15/s |
| acquire, 자리 1 | 10 | 0 | 1 | 9 | ≈2,000ms | ≈5/s |
| acquire, 자리 10 | 10 | 0 | 10 | 0 | ≈200ms | ≈50/s |

- `maxRunning`은 어떤 모드에서도 자리 수를 넘지 않는다.
- 거절·시한부가 처리량은 더 높게 나온다. 그러나 그건 일을 버려서 분모(시간)가 줄어든 결과다.
- 시한부 대기는 대기 시간이 작업 시간보다 짧으면 거절과 똑같아진다.

## Experiment

`SemaphoreThrottle.java` 하나. 스레드 10개를 `new Thread` + `start` + `join`으로 돌리고, 각 스레드는

1. `tryAcquire()`로 자리를 살핀다 →
2. 없으면 모드에 따라 거절 / `tryAcquire(waitMs)` / `acquire()` →
3. 자리를 얻은 스레드만 `Thread.sleep(workMs)`로 일한다 →
4. `finally`에서 `release()`.

세는 값: `done`, `rejected`, `maxRunning`(동시 실행 최대), `maxWaiting`(대기 줄 최대), 총 시간, 처리량(= done / 총 시간).

실행 인자 `[mode] [permits] [threads] [workMs] [waitMs]`, 기본값 `acquire 3 10 200 300`. 모드 오타는 예외.

| 순서 | 인자 | 회차 |
|---|---|---|
| 1 | `acquire` | 3 |
| 2 | `try` | 3 |
| 3 | `timed` | 3 |
| 4 | `timed 3 10 200 100` | 1 |
| 5 | `acquire 1 10 200` | 1 |
| 6 | `acquire 10 10 200` | 1 |

## Result

원본 콘솔 출력은 회사 폐쇄망에서 텍스트 파일에 모아 사진으로 남겼다.

```
mode=acquire permits=3 threads=10 workMs=200 waitMs=300
[acquire] done=10 rejected=0 maxRunning=3 maxWaiting=7 elapsed=836ms throughput=12.0/s
[acquire] done=10 rejected=0 maxRunning=3 maxWaiting=7 elapsed=846ms throughput=11.8/s
[acquire] done=10 rejected=0 maxRunning=3 maxWaiting=7 elapsed=845ms throughput=11.8/s
mode=try permits=3 threads=10 workMs=200 waitMs=300
[try    ] done=3 rejected=7 maxRunning=3 maxWaiting=0 elapsed=215ms throughput=14.0/s
[try    ] done=3 rejected=7 maxRunning=3 maxWaiting=0 elapsed=215ms throughput=14.0/s
[try    ] done=3 rejected=7 maxRunning=3 maxWaiting=0 elapsed=215ms throughput=14.0/s
mode=timed permits=3 threads=10 workMs=200 waitMs=300
[timed  ] done=6 rejected=4 maxRunning=3 maxWaiting=7 elapsed=426ms throughput=14.1/s
[timed  ] done=6 rejected=4 maxRunning=3 maxWaiting=7 elapsed=426ms throughput=14.1/s
[timed  ] done=6 rejected=4 maxRunning=3 maxWaiting=7 elapsed=426ms throughput=14.1/s
mode=timed permits=3 threads=10 workMs=200 waitMs=100
[timed  ] done=3 rejected=7 maxRunning=3 maxWaiting=7 elapsed=215ms throughput=14.0/s
mode=acquire permits=1 threads=10 workMs=200 waitMs=300
[acquire] done=10 rejected=0 maxRunning=1 maxWaiting=9 elapsed=2105ms throughput=4.8/s
mode=acquire permits=10 threads=10 workMs=200 waitMs=300
[acquire] done=10 rejected=0 maxRunning=10 maxWaiting=0 elapsed=219ms throughput=45.7/s
```

### 요약

| 실행 | done | rejected | maxRunning | maxWaiting | elapsed | throughput | 가설 |
|---|---|---|---|---|---|---|---|
| acquire ×3 | 10 | 0 | 3 | 7 | 836~846ms | 11.8~12.0/s | 일치 |
| try ×3 | 3 | 7 | 3 | 0 | 215ms | 14.0/s | 일치 |
| timed 300 ×3 | 6 | 4 | 3 | 7 | 426ms | 14.1/s | 일치 |
| timed 100 | 3 | 7 | 3 | 7 | 215ms | 14.0/s | 일치 |
| 자리 1 | 10 | 0 | 1 | 9 | 2,105ms | 4.8/s | 일치 |
| 자리 10 | 10 | 0 | 10 | 0 | 219ms | 45.7/s | 일치 |

같은 조건의 회차끼리는 ms 단위까지 거의 같았다(try 세 번 모두 215ms, timed 세 번 모두 426ms).

## What I learned

### 1. 세마포어 초기값 = 동시에 일하는 수의 상한
여섯 실행 모두 `maxRunning`이 자리 수와 정확히 같았다(3, 1, 10). 초기값은 "바로 나눠줄 자리 수"이고(OSTEP 31), 자리를 돌려주기 전에는 누구도 더 들어오지 못한다. 자리 1은 락이고(`lock_implementations`의 `reentrant`와 같은 모양, 2,105ms = 10 × 210ms 직렬), 자리 10은 제한이 없는 것과 같다(219ms = 한 바퀴).

### 2. 넘치는 요청의 운명은 세 가지뿐이고, 그게 백프레셔의 세 손잡이다
자리가 다 나갔을 때 세마포어가 할 수 있는 일은 **기다리게 하거나, 돌려보내거나, 잠깐만 기다리게** 하는 것이다. 세 모드는 같은 10개 요청을 받아 다르게 처리했다.

| 손잡이 | 완료 | 버림 | 마지막 요청의 대기 | 대기 줄 |
|---|---|---|---|---|
| 대기 (`acquire`) | 10 | 0 | ≈630ms (세 바퀴) | 7까지 자람 |
| 거절 (`tryAcquire`) | 3 | 7 | 0 | 없음 |
| 시한부 (`tryAcquire(300ms)`) | 6 | 4 | ≤300ms | 7까지 자랐다가 300ms에 비워짐 |

- **대기**는 일을 하나도 안 버리지만, 대기 줄 길이와 지연에 상한이 없다. 요청이 계속 들어오면 줄은 무한히 자란다.
- **거절**은 지연이 0이지만 70%를 버렸다. 서버가 살아남는 대신 호출자가 실패를 받는다.
- **시한부**는 "300ms까지는 기다려 주되 그 뒤엔 버린다"는 지연 상한이다. 첫 묶음이 215ms에 자리를 비우자 대기 중 3개가 들어갔고, 나머지 4개는 300ms에서 포기했다(426ms = 두 번째 묶음이 끝난 시각).

### 3. 시한부 대기 시간의 기준은 "작업 한 바퀴보다 긴가"
대기 100ms는 거절과 결과가 똑같았다(3/7/215ms). 자리는 빨라야 작업 시간(200ms) 뒤에 나는데 100ms만 기다리면 자리가 나기 전에 모두 포기한다. 시한부 대기 시간은 임의의 숫자가 아니라 **자리가 돌아오는 주기(작업 시간)** 에 대고 정해야 의미가 있다. 300ms는 한 바퀴(200ms)는 넘고 두 바퀴(400ms)는 못 미쳐서 정확히 한 묶음만 더 받았다.

### 4. 처리량이 높은 쪽이 좋은 쪽이 아니다
거절(14.0/s)과 시한부(14.1/s)가 대기(11.8/s)보다 처리량이 높게 나왔다. 그러나 그건 분자(완료)가 늘어서가 아니라 **일을 버려서 분모(총 시간)가 줄어든** 결과다. 완료 수는 10 > 6 > 3으로 정반대다. "초당 몇 개"는 버린 일을 안 세기 때문에, 과부하 정책을 비교할 때는 처리량이 아니라 **완료 수·버린 수·최대 지연** 세 개를 같이 봐야 한다. 처리량 ≠ 처리 품질.

### 5. 시간이 200ms가 아니라 215ms인 이유
`Thread.sleep(200)`이 215ms로 끝났다. Windows의 기본 타이머 해상도가 약 15.6ms라 잠든 스레드는 다음 틱에 깨어난다. acquire의 846ms도 4 × 211ms로, 묶음마다 같은 오차가 누적된 값이다. 회차 간 흔들림이 거의 없는 것도 이 실험이 CPU 경쟁이 아니라 타이머에 묶여 있기 때문이다.

### 한 줄 결론
**세마포어 초기값은 동시 실행 수의 상한이고, 넘치는 요청은 기다리거나·버려지거나·잠깐 기다리다 버려지는 세 길뿐이다. 어느 길이 좋은지는 처리량이 아니라 "얼마나 버리고 얼마나 기다리게 할 것인가"로 정한다.**

### 내가 느낀 점
세마포어 허가 수는 동시에 일하는 수의 상한이었고, 넘치는 요청은 기다리거나 · 버려지거나 · 잠깐 기다리다 버려지는 세 길뿐이었다. 거절은 처리량이 가장 높았지만 일을 70% 버린 결과라, 처리량 하나로 정책을 고르면 안 된다. 다만 뒤쪽 DB나 외부 API가 버티지 못한다면 앞에서 거절하는 게 오히려 시스템을 지키는 길이 될 수 있다. 결국 어떤 손잡이를 쓸지는 기술이 아니라 **그 기능을 쓰는 쪽이 무엇을 못 참느냐**(일이 사라지는 것, 오래 기다리는 것, 뒤쪽이 무너지는 것)에 따라 정책으로 정해진다. 세마포어 안도 결국 CAS 한 번이라, 앞의 두 실험과 같은 바닥 위에서 기다리는 방식만 다르다.

### 이 실험의 한계
- 작업이 `sleep`이라 CPU를 쓰지 않는다. 실제 작업(I/O 대기, 계산)이면 묶음 경계가 이렇게 깔끔하지 않다.
- 스레드 10개가 한 번에 출발하는 모양이라 "요청이 계속 들어오는" 상황(도착률 > 처리율)의 대기 줄 성장은 보지 못했다. → 2번 `bounded_queue_policy`
- `tryAcquire()`로 먼저 살핀 뒤 모드별 대기로 들어가는 두 단계는 원자적이지 않다. 사이에 자리가 나면 그 스레드는 "대기 줄에 들어갔다가 곧바로 얻은" 것으로 센다. `maxWaiting` 집계에만 영향이 있고 완료·거절 수에는 영향이 없다.
- 공정성(`new Semaphore(n, true)`)은 보지 않았다. 대기 중 7개 중 누가 먼저 자리를 받는지는 비공정 기본값대로 임의다.

## Next question

- [ ] 요청이 일정 간격으로 계속 들어오게 바꾸면(도착률 > 처리율) `acquire`의 대기 줄은 정말 끝없이 자라나? → `java/bounded_queue_policy` (스레드 풀의 거부 정책 = 같은 세 손잡이)
- [ ] `new Semaphore(3, true)`(공정)로 바꾸면 대기 7개가 들어온 순서대로 자리를 받나? 총 시간은?
- [ ] `tryAcquire(timeout)` 대기 중인 스레드는 `jcmd <PID> Thread.print`에서 `TIMED_WAITING (parking)`으로 보이나? → `java/reentrantlock_state`와 같은 방법
- [ ] 작업을 `sleep` 대신 실제 계산(예: 큰 배열 정렬)으로 바꾸면 코어 4개에서 자리 3 vs 자리 10의 총 시간 차이는 어떻게 되나?
