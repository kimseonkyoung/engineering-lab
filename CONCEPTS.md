# Concepts — 개념 색인

[`BLUEPRINT.md`](BLUEPRINT.md)의 마디별로 개념 카드를 모은다. 나중에 꺼내 볼 때는 이 파일 하나만 본다.

- 상태: `□` 미착수 · `◐` 진행 중 · `■` 완료
- 축: ① 이직 · ② 업무 · ③ 미래 직무
- ★ 마디의 대표 실험 — 시간이 없으면 이것만, 나머지는 이직 후에
- 완료된 실험은 아래 **카드 틀**로 적는다. 미착수는 한 줄만 둔다.

```
### 개념 이름 ■
- 정의: 한 문장
- 왜 중요한가: 어느 축의 어떤 문제에서 다시 만나는가
- 실험: 폴더 → 한 줄 결론
- 재현 조건: 환경, 바꾸면 결과가 달라지는 변수
- 다음 마디로 가는 질문
```

---

## A1 스레드와 스케줄링

| 상태 | 개념 | 질문 | 실험 (예정 폴더) | 축 |
|---|---|---|---|---|
| □ | 스레드 생명주기 | `start()`하면 OS 스레드가 정말 생기나? 스레드 덤프와 OS 프로세스 뷰에서 같은 스레드를 찾을 수 있나? 상태는 언제 바뀌나? | `java/thread_lifecycle` | ①② |
| □ | 스레드 생성 비용 ★ | 스레드 1개의 시간·메모리·스택 비용은? 1만 개를 만들면 어디서 터지나? | `java/thread_cost` | ①③ |
| □ | 풀과 재사용 | 미리 만든 일꾼을 재사용하는 것과 매번 만드는 것의 차이는 어디서 나나? | `java/thread_pool_vs_raw` | ② |
| □ | 문맥 교환 | 코어 1개에 스레드 100개가 돌면 각각 얼마나 자주 끊기나? 끊김 비용은? | `cs/context_switch` | ① |
| □ | 가상 스레드 | 위 비용을 가상 스레드는 어떻게 바꾸나? (집, JDK 21) | `java/virtual_threads` | ①③ |

## A2 동기화

### 잃어버린 갱신과 JIT ■
- 정의: 읽기·더하기·쓰기 사이의 틈에 다른 스레드가 끼어들면 갱신이 사라진다. JIT는 그 틈이 열리는 횟수를 줄여 버그를 숨길 수 있다.
- 왜 중요한가: ① 면접 "테스트 통과가 안전의 증거가 아닌 이유" / ② 공유 카운터·캐시를 다루는 모든 코드
- 실험: [`cs/race_condition`](cs/race_condition/) → 락 없는 증가는 갱신을 잃고, JIT는 숨길 뿐이며, `volatile`은 원자성을 주지 않는다
- 재현 조건: JIT 켜짐/`-Xint`, 동시 출발 여부, 반복 횟수. JDK 8 x86-64
- 다음 질문: JIT가 실제로 만든 기계어는 무엇인가 (`-XX:+PrintCompilation`)

### 락의 구조: 깃발 하나와 원자 명령 ■
- 정의: 락은 깃발 변수 하나를 CAS로 바꾸는 것이고, CAS의 원자성은 CPU가 캐시 줄을 독점한 채 읽기~쓰기를 끝내는 데서 온다. 속도는 못 잡았을 때 무엇을 하느냐(돌기·재우기·순서 지키기)로 갈린다.
- 왜 중요한가: ① `synchronized`와 `ReentrantLock`의 차이를 "기다리는 방식"으로 설명 / ③ 프록시의 동시 요청 제어
- 실험: [`cs/lock_implementations`](cs/lock_implementations/) → naive 깃발만 손실, 나머지는 정답이나 속도는 수십 배 차이
- 재현 조건: 스레드 2개 경쟁, N=100만. `fair`는 N에 따라 수십 초
- 다음 질문: TTAS로 바꾸면 spin이 reentrant만큼 빨라지나

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| ◐ | 세마포어와 백프레셔 ★ | 초기값 N이 동시 실행 수를 정하나? 대기·거절·시간 제한 대기는 완료·거절·대기열·처리량이 어떻게 다른가? | `cs/semaphore_throttle` | ①②③ |
| □ | 기다리는 방식의 비용 | TTAS, `yield`, 티켓 락은 spin과 무엇이 다른가 | `cs/ttas_spin_lock` | ① |
| □ | 잠드는 락 | `park`/`unpark`로 만든 락은 스핀 락과 비용이 어떻게 다른가 (OSTEP 28.13~16) | `cs/parking_lock` | ① |
| □ | `ReentrantLock`과 AQS | 상태·소유자·대기열이 실제로 어떻게 보이나, 재진입은 어떻게 세나 | `java/reentrantlock_state` | ① |
| □ | `synchronized` 단계 | 경량→중량 전환은 언제 일어나고 스레드 덤프에서 어떻게 보이나 | `java/synchronized_stages` | ① |
| □ | 가시성과 선행 관계 | `volatile` 없는 종료 플래그는 왜 안 멈추나, 락은 왜 값을 전달하나 | `java/visibility_flag` | ①② |

## A3 I/O와 이벤트 루프
_마디 도달 시 설계_

## A4 백프레셔와 흐름 제어

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 토큰 버킷 | 순간 폭주는 허용하고 평균은 제한하는 방법 | `cs/token_bucket` | ②③ |
| □ | 유한 큐와 거부 정책 ★ | 큐가 차면 누가 대가를 치르나 (호출자 실행, 버림, 예외) | `java/bounded_queue_policy` | ②③ |

## B1 모델이 하는 일

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 온도와 샘플링 ★ | 로짓에 온도를 적용하면 분포가 어떻게 변하나 | `ai/temperature_sampling` | ②③ |
| □ | 교차 엔트로피·퍼플렉서티 | "덜 놀란다"는 숫자로 무엇인가 | `ai/cross_entropy` | ② |
| □ | n-그램 엔트로피 | 문맥이 길수록 다음 글자 엔트로피가 줄어드나 (섀넌 1951) | `ai/ngram_entropy` | ③ |
| □ | 벡터 유사도 | 정규화 벡터에서 코사인·내적·유클리드 순위가 같은가 | `ai/vector_similarity` | ② |
| □ | 임계값과 정밀도·재현율 | 임계값을 움직이면 둘이 어떻게 맞바뀌나 | `ai/precision_recall` | ② |
| □ | 행렬곱·PCA 직접 구현 | 손계산과 맞나, 반복문 순서가 속도를 바꾸나 | `ai/matrix_ops` | ③ |
| □ | 파라미터와 과적합 | 같은 점에 직선·곡선을 맞추면 언제 외워 버리나 | `ai/overfit_curve` | ③ |

## D1 클래스 로딩과 링킹

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 부모 위임 | 내 클래스와 같은 이름의 `java.lang.String`을 만들면 누가 이기나 | `java/classloader_delegation` | ① |
| □ | 로더별 클래스 정체성 ★ | 같은 `.class`를 로더 둘로 올리면 `instanceof`와 캐스팅이 왜 깨지나 | `java/classloader_identity` | ①③ |
| □ | 초기화 시점 | `static` 블록은 로딩 때가 아니라 언제 실행되나 (`Class.forName` vs `loadClass`, 상수 접근) | `java/class_init_timing` | ① |

## D2 바이트코드와 호출

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 스택 머신 | `a = b + c`가 어떤 명령 네 개가 되나, 지역 변수 슬롯은 어떻게 쓰이나 | `java/bytecode_reading` | ① |
| □ | 호출 네 종류 | `invokevirtual`·`invokeinterface`·`invokestatic`·`invokedynamic`은 디스패치가 어떻게 다르고 비용 차이는 얼마나 되나 | `java/invoke_kinds` | ① |
| □ | 람다의 정체 ★ | 람다는 왜 익명 클래스가 아니라 `invokedynamic`이고, 처음 호출 때 무슨 일이 생기나 | `java/lambda_indy` | ① |
| □ | 상수 풀과 `intern` | 같은 문자열 리터럴이 왜 `==`로 같고, `new String`은 왜 다른가 | `java/string_intern` | ① |

## D3 리플렉션과 메서드 핸들

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 리플렉션 호출 비용 ★ | `Method.invoke`는 1회와 1만 회에서 비용이 왜 다른가 (인플레이션 임계) | `java/reflection_cost` | ① |
| □ | 접근 제어 우회 | `setAccessible`로 `private`에 닿을 때 무엇이 검사되고, JDK 9+ 모듈은 무엇을 막나 | `java/reflection_access` | ①③ |
| □ | 메서드 핸들 | `MethodHandle`은 리플렉션과 무엇이 다르고 JIT가 왜 더 잘 최적화하나 | `java/method_handle` | ① |
| □ | 다이내믹 프록시 | C6과 같은 실험 (`spring/dynamic_proxy`) | — | ①③ |

## D4 JIT와 최적화

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 뜨거운 코드 판정 ★ | 호출 횟수·백엣지 횟수가 기준을 넘으면 무엇이 찍히나, OSR은 언제 일어나나 (`-XX:+PrintCompilation`) | `java/jit_compilation_log` (race_condition 후속) | ① |
| □ | 인라이닝 | 작은 메서드는 언제 인라인되고, 깊이·크기 한계를 넘으면 어떻게 느려지나 | `java/jit_inlining` | ① |
| □ | 탈최적화 | 한 번도 안 가던 분기를 실행하면 컴파일된 코드가 어떻게 버려지나 | `java/deoptimization` | ① |
| □ | 탈출 분석 | 메서드 밖으로 안 나가는 객체는 힙 할당이 사라지나 | `java/escape_analysis` | ① |

## D5 메모리와 GC

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 객체 크기와 정렬 | 필드 순서와 헤더 때문에 객체가 몇 바이트가 되나 (JOL 없이 간접 측정) | `java/object_layout` | ① |
| □ | 세대별 GC ★ | 짧게 사는 객체와 오래 사는 객체가 GC 로그에서 어떻게 다르게 보이나 | `java/gc_generations` | ①② |
| □ | 참조 종류 | 약한·소프트·팬텀 참조는 각각 언제 회수되나 | `java/reference_types` | ① |
| □ | 클래스 로더 누수 | 로더를 계속 만들면 메타스페이스가 왜 터지나 (D1 연결) | `java/metaspace_leak` | ①② |

## D6 자바 메모리 모델

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 선행 관계 | A2의 `visibility_flag`와 공유 | — | ① |
| □ | 안전하지 않은 공개 | 생성자가 끝나기 전 객체가 다른 스레드에 보이면 무엇이 깨지나, `final`은 왜 안전한가 | `java/unsafe_publication` | ① |
| □ | 이중 검사 잠금 ★ | `volatile` 없는 DCL이 왜 깨지는가 | `java/double_checked_locking` | ① |

## D7 핫스왑과 인스트루멘테이션

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 디버거 핫스왑 | 메서드 본문만 바뀌는 이유, 필드·시그니처를 바꾸면 왜 거부되나 | `java/hotswap_limits` | ① |
| □ | 자바 에이전트 ★ | `java.lang.instrument`로 클래스를 로딩 시점에 바꾸면 무엇이 가능한가 | `java/instrument_agent` | ①③ |
| □ | 재시작 vs 재로딩 | 스프링 데브툴이 로더 두 개를 쓰는 이유 (D1 연결) | `spring/devtools_classloaders` | ① |

## D8 가상 스레드
_A1의 `java/virtual_threads`와 공유._

## E1 컬렉션 내부

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | `HashMap` 구조 ★ | 버킷은 언제 늘어나고(리사이즈), 충돌이 쌓이면 언제 트리가 되나, 노드 하나는 몇 바이트인가(D5 연결) | `java/hashmap_internals` | ①② |
| □ | `equals`·`hashCode` 계약 | 하나만 재정의하면 맵에서 무슨 일이 생기나, 가변 키를 넣고 바꾸면? | `java/hashcode_contract` | ① |
| □ | 리스트 두 종류 | `ArrayList` 증가 전략, `LinkedList`가 중간 삽입에서도 느린 이유(캐시 지역성) | `java/list_locality` | ① |
| □ | 정렬 맵과 LRU | `TreeMap` 레드블랙 트리 깊이, `LinkedHashMap` 접근 순서로 LRU 만들기 | `java/treemap_lru` | ①② |

## E2 동시 컬렉션

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | `ConcurrentHashMap` | JDK 8에서 버킷 단위 CAS와 `synchronized`를 어떻게 섞나, `size()`는 왜 근사값인가 | `java/chm_internals` | ①② |
| □ | 실패 빠름 ★ | `ConcurrentModificationException`은 무엇을 세서 던지고 왜 "가끔"만 나나 | `java/fail_fast_iterator` | ①② |
| □ | 복사 후 쓰기 | `CopyOnWriteArrayList`는 읽기가 왜 락 없이 안전하고 쓰기는 얼마나 비싼가 | `java/copy_on_write` | ① |
| □ | 블로킹 큐 종류 | `ArrayBlockingQueue`·`LinkedBlockingQueue`·`SynchronousQueue`는 생산자·소비자를 어떻게 다르게 묶나 | `java/blocking_queues` | ①② |

## E3 실행기와 비동기

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 스레드 풀 매개변수 ★ | 코어·큐·최대 스레드는 어떤 순서로 차나 (큐가 먼저 찬다) | `java/threadpool_params` | ①② |
| □ | 거부 정책 | 큐도 최대도 찼을 때 4가지 정책은 누가 대가를 치르나 (A4 `bounded_queue_policy`와 공유) | — | ①②③ |
| □ | `CompletableFuture` 스레드 | 콜백은 어느 스레드에서 도나, `thenApply`와 `thenApplyAsync`는 무엇이 다른가 | `java/completable_future_threads` | ①② |
| □ | 작업 훔치기 | `ForkJoinPool`은 큐를 어떻게 나누고, 병렬 스트림이 공통 풀을 쓰면 무엇이 위험한가 | `java/forkjoin_stealing` | ① |

## E4 동기화 도구

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 래치와 배리어 | `CountDownLatch`는 왜 한 번뿐이고 `CyclicBarrier`는 어떻게 재사용되나 | `java/latch_vs_barrier` | ① |
| □ | 조건 변수 ★ | `Condition`으로 유한 버퍼를 만들면 `while`로 다시 확인해야 하는 이유 (OSTEP 30장) | `java/condition_bounded_buffer` | ① |
| □ | 읽기·쓰기 락 | `ReadWriteLock` vs `StampedLock` 낙관적 읽기는 언제 이기나 | `java/rwlock_vs_stamped` | ① |

## E5 제네릭과 타입 소거

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 소거 ★ | `List<String>`과 `List<Integer>`가 런타임에 같은 클래스라는 것을 어떻게 확인하나 (`javap`, D2 연결) | `java/type_erasure` | ① |
| □ | 브리지 메서드 | 제네릭 메서드를 오버라이드하면 컴파일러가 왜 메서드를 하나 더 만드나 | `java/bridge_method` | ① |
| □ | 공변과 불변 | 배열은 왜 `Object[]`에 `String[]`을 넣을 수 있고 제네릭은 왜 안 되나 | `java/array_covariance` | ① |
| □ | 와일드카드 | `? extends`와 `? super`는 각각 무엇을 막나 (PECS) | `java/wildcard_pecs` | ① |

## E6 스트림과 람다 내부

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 지연 평가 ★ | 중간 연산은 언제 실행되고 요소 하나가 파이프라인을 끝까지 가나 | `java/stream_laziness` | ① |
| □ | 박싱 비용 | `Stream<Integer>`와 `IntStream`의 할당 차이 | `java/stream_boxing` | ① |
| □ | 람다 캡처 | 캡처된 변수는 복사되나 참조되나, 왜 사실상 `final`이어야 하나 | `java/lambda_capture` | ① |

## E7 I/O와 NIO 버퍼

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 버퍼 상태 ★ | `position`·`limit`·`capacity`가 `flip`·`compact`·`clear`에서 어떻게 움직이나 | `java/bytebuffer_states` | ①③ |
| □ | 다이렉트 버퍼 | 힙 버퍼로 쓰면 복사가 한 번 더 일어나나, 다이렉트 버퍼는 GC 밖에서 어떻게 관리되나 | `java/direct_buffer` | ①③ |
| □ | 채널 | 파일 채널 읽기와 스트림 읽기는 무엇이 다른가 (A3 진입 전 준비) | `java/channel_vs_stream` | ① |

## E8 불변·직렬화·레코드

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 방어적 복사 | 필드가 `final`이어도 가변 객체면 왜 깨지나 (이펙티브 자바 연결) | `java/defensive_copy` | ① |
| □ | 직렬화 함정 | `Serializable`은 생성자를 건너뛴다는 것을 확인, 싱글톤이 깨지는 경우 | `java/serialization_pitfalls` | ① |
| □ | 레코드 | 레코드가 만들어 주는 메서드와 못 하는 것 (집, JDK 17+) | `java/records` | ① |

## E9 언어 변천
_집 JDK 21에서: `var`, 봉인 클래스, 스위치 패턴, 텍스트 블록. 도달 시 설계._

## C1 관심사 분리와 의존 방향

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 상속의 함정 | 부모 생성자에서 오버라이딩된 메서드를 부르면 무슨 일이 생기나, 필드·정적 메서드는 어느 쪽을 따르나 | `java/inheritance_pitfalls` | ① |
| □ | 상속 분리 vs 인터페이스 분리 ★ | 조합이 늘 때 클래스는 몇 개 늘고 몇 개를 고쳐야 하나 | `spring/toby_01_dao_separation` | ① |
| □ | 제어의 역전 | 조립 책임을 팩토리로 빼면 고칠 곳이 어디로 모이나 | `spring/toby_01_dao_separation` (같은 실험 2회차) | ①③ |
| □ | 싱글톤 레지스트리 | 팩토리 직접 호출과 컨테이너 `getBean`은 같은 객체를 주나 | `spring/toby_01_singleton` | ① |

## C6 프록시와 횡단 관심사

| 상태 | 개념 | 질문 | 실험 | 축 |
|---|---|---|---|---|
| □ | 다이내믹 프록시 | `java.lang.reflect.Proxy`로 호출을 가로채면 무엇이 보이고 무엇이 안 보이나 | `spring/dynamic_proxy` | ①③ |
| □ | 자기 호출 ★ | 같은 객체 안에서 부른 메서드에는 왜 프록시가 안 걸리나 | `spring/self_invocation` | ① |

_나머지 마디(A5~A8, B2~B5, C2~C5, C7~C8, E9)는 도달 시 설계. D1~D7과 E1~E8은 이직 면접 전 우선순위라 미리 채움._
