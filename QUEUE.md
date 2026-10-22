# Queue — 실험 대기열

순서와 내용은 기술 과외에서 정하고, 날짜는 일정 담당이 붙인다. 끝난 실험은 `■`로 바꾸고 [`CONCEPTS.md`](CONCEPTS.md) 카드를 채운다.

## 이직 전 (대표 20 + 흥미 2)

| 순번 | 상태 | 실험 | 마디 | 장소 | 시간 | 먼저 읽을 것 |
|---|---|---|---|---|---|---|
| 1 | ◐ | `cs/semaphore_throttle` | A2·A4 | 회사 | 40분 + 집 30분 | OSTEP 31.1~31.3, 31.7 |
| 2 | □ | `java/bounded_queue_policy` | A4·E3 | 회사 | 40분 | 없음 (1번 결과) |
| 3 | □ | `java/threadpool_params` | E3 | 회사 | 40분 | 없음 (2번 결과) |
| 4 | □ | `java/thread_cost` | A1 | 회사 | 40분 | OSTEP 26.1~26.3 (집 30분) |
| 5 | □ | `java/thread_lifecycle` (흥미) | A1 | 회사 | 30분 | 없음 |
| 6 | □ | `java/condition_bounded_buffer` | E4 | 회사 | 40분 | OSTEP 30장 앞부분 (집 30분) |
| 7 | □ | `java/jit_compilation_log` | D4 | 회사 | 30분 | 없음 (race_condition 후속) |
| 8 | □ | `java/double_checked_locking` | D6 | 회사 | 40분 | 없음 |
| 9 | □ | `java/hashmap_internals` | E1 | 회사 | 40분 × 2회 | 없음 |
| 10 | □ | `java/fail_fast_iterator` | E2 | 회사 | 30분 | 없음 |
| 11 | □ | `spring/toby_01_dao_separation` | C1 | 회사 | 40분 × 2회 | 토비 1.1~1.4 (집) |
| 12 | □ | `java/classloader_identity` | D1 | 회사 | 40분 | 없음 |
| 13 | □ | `java/lambda_indy` | D2 | 회사 | 30분 | 없음 |
| 14 | □ | `java/reflection_cost` | D3 | 회사 | 40분 | 없음 |
| 15 | □ | `java/type_erasure` | E5 | 회사 | 30분 | 없음 |
| 16 | □ | `java/gc_generations` | D5 | 회사 | 40분 | 없음 |
| 17 | □ | `java/stream_laziness` | E6 | 회사 | 30분 | 없음 |
| 18 | □ | `java/bytebuffer_states` | E7 | 회사 | 40분 | 없음 |
| 19 | □ | `spring/self_invocation` | C6 | 회사 | 40분 | 토비 6장 (집) |
| 20 | □ | `java/instrument_agent` | D7 | 회사 | 40분 × 2회 | 없음 |
| 21 | □ | `ai/temperature_sampling` | B1 | 회사 | 40분 | AI 엔지니어링 2장 |
| 22 | □ | `ai/ngram_entropy` (흥미) | B1 | 집 | 1시간 | 섀넌 1951 앞부분 |

## 제약

- 회사 실험은 JDK 8 + Eclipse, 한 번에 30~40분. "× 2회"는 이틀로 나눈다.
- 11번·19번은 토비 읽기 진도에 묶임. 토비 1장은 11번 앞, 6장은 19번 앞.
- 1·4·6번은 집에서 읽기 30분이 먼저.
- 순서는 바꿔도 되지만 1→2→3, 4→5→6, 9→10, 12→13→14→15는 묶음 안에서 순서 유지.
- 22번만 집(리눅스, 텍스트 파일).

## 이직 후

[`CONCEPTS.md`](CONCEPTS.md)의 ★ 없는 항목. 도달 시 여기에 순번을 붙인다.
