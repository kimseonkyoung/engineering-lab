import java.lang.management.ManagementFactory;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 세마포어로 동시 실행 수를 제한할 때, 넘치는 요청을 어떻게 다루느냐에 따른 차이.
 *
 *   acquire : 자리가 날 때까지 기다린다           (대기)
 *   try     : 자리가 없으면 바로 포기한다          (거절)
 *   timed   : 일정 시간만 기다리고 없으면 포기한다  (시한부 대기)
 *
 * 실행 인자: [mode] [permits] [threads] [workMs] [waitMs]
 *   예) acquire              → 기본값 3 10 200 300
 *       timed 3 10 200 100
 *
 * 한 번 실행하면 ROUNDS회 반복해 결과 줄을 찍는다.
 * (실험 당일 회사에서 쓴 판은 1회만 돌고, 실행마다 인자를 바꿔 넣었다.)
 */
public class SemaphoreThrottle {
    static final int ROUNDS = 3;

    public static void main(String[] args) throws InterruptedException {
        String mode = args.length > 0 ? args[0] : "acquire";
        if (!mode.equals("acquire") && !mode.equals("try") && !mode.equals("timed")) {
            throw new IllegalArgumentException("mode must be acquire, try, or timed: " + mode);
        }
        int permits = args.length > 1 ? Integer.parseInt(args[1]) : 3;
        int threads = args.length > 2 ? Integer.parseInt(args[2]) : 10;
        int workMs  = args.length > 3 ? Integer.parseInt(args[3]) : 200;
        int waitMs  = args.length > 4 ? Integer.parseInt(args[4]) : 300;

        printEnv();
        System.out.printf("mode=%s permits=%d threads=%d workMs=%d waitMs=%d%n",
            mode, permits, threads, workMs, waitMs);
        for (int round = 1; round <= ROUNDS; round++) {
            run(mode, permits, threads, workMs, waitMs);
        }
    }

    static void run(final String mode, int permits, int threads, final int workMs, final int waitMs)
            throws InterruptedException {
        final Semaphore sem = new Semaphore(permits);
        final AtomicInteger running = new AtomicInteger(), maxRunning = new AtomicInteger();
        final AtomicInteger waiting = new AtomicInteger(), maxWaiting = new AtomicInteger();
        final AtomicInteger done = new AtomicInteger(), rejected = new AtomicInteger();

        Runnable job = () -> {
            boolean got = sem.tryAcquire();                 // 1) 일단 자리 있나 본다
            if (!got) {
                if (mode.equals("try")) {                   // 2a) 거절: 바로 포기
                    rejected.incrementAndGet();
                    return;
                }
                maxWaiting.accumulateAndGet(waiting.incrementAndGet(), Math::max);
                try {
                    if (mode.equals("timed")) {             // 2b) 시한부 대기
                        got = sem.tryAcquire(waitMs, TimeUnit.MILLISECONDS);
                    } else {                                // 2c) 무기한 대기
                        sem.acquire();
                        got = true;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                waiting.decrementAndGet();
                if (!got) {
                    rejected.incrementAndGet();
                    return;
                }
            }
            try {                                           // 3) 자리를 얻은 스레드만 일한다
                maxRunning.accumulateAndGet(running.incrementAndGet(), Math::max);
                Thread.sleep(workMs);
                done.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                running.decrementAndGet();
                sem.release();                              // 4) 반드시 돌려준다
            }
        };

        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(job, "worker-" + i);
            workers[i].start();
        }
        for (Thread t : workers) t.join();
        long ms = (System.nanoTime() - start) / 1_000_000;

        System.out.printf("[%-7s] done=%d rejected=%d maxRunning=%d maxWaiting=%d elapsed=%dms throughput=%.1f/s%n",
            mode, done.get(), rejected.get(), maxRunning.get(), maxWaiting.get(), ms,
            ms == 0 ? 0.0 : done.get() * 1000.0 / ms);
    }

    static void printEnv() {
        System.out.printf("JDK=%s (%s), OS=%s %s, cores=%d, JVM args=%s%n",
            System.getProperty("java.version"), System.getProperty("java.vendor"),
            System.getProperty("os.name"), System.getProperty("os.version"),
            Runtime.getRuntime().availableProcessors(),
            ManagementFactory.getRuntimeMXBean().getInputArguments());
    }
}
