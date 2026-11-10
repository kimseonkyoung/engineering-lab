import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 일꾼 2명 + 대기열 N칸짜리 스레드 풀에, 처리 속도보다 빠르게 일을 계속 넣는다.
 * 대기열이 가득 찼을 때의 거부 정책에 따라 "누가 대가를 치르나"를 본다.
 *
 *   abort   : 넣는 쪽에 예외를 던진다            (ThreadPoolExecutor 기본값)
 *   caller  : 넣는 쪽이 그 일을 직접 한다
 *   discard : 새로 온 일을 조용히 버린다
 *   oldest  : 대기열에서 가장 오래 기다린 일을 버리고 새 일을 넣는다
 *
 * 실행 인자: [mode] [queueCap] [tasks] [intervalMs] [workMs]
 *   기본값   abort 3 20 10 100
 *   queueCap 에 0 이하를 주면 크기 제한 없는 대기열(LinkedBlockingQueue)
 */
public class BoundedQueuePolicy {
    static final int WORKERS = 2;

    public static void main(String[] args) throws InterruptedException {
        String mode = args.length > 0 ? args[0] : "abort";
        int queueCap   = args.length > 1 ? Integer.parseInt(args[1]) : 3;
        int tasks      = args.length > 2 ? Integer.parseInt(args[2]) : 20;
        int intervalMs = args.length > 3 ? Integer.parseInt(args[3]) : 10;
        final int workMs = args.length > 4 ? Integer.parseInt(args[4]) : 100;

        RejectedExecutionHandler policy;
        switch (mode) {
            case "abort":   policy = new ThreadPoolExecutor.AbortPolicy(); break;
            case "caller":  policy = new ThreadPoolExecutor.CallerRunsPolicy(); break;
            case "discard": policy = new ThreadPoolExecutor.DiscardPolicy(); break;
            case "oldest":  policy = new ThreadPoolExecutor.DiscardOldestPolicy(); break;
            default: throw new IllegalArgumentException("mode must be abort, caller, discard, or oldest: " + mode);
        }
        BlockingQueue<Runnable> queue = queueCap > 0
            ? new ArrayBlockingQueue<Runnable>(queueCap)
            : new LinkedBlockingQueue<Runnable>();

        printEnv();
        System.out.printf("mode=%s workers=%d queueCap=%s tasks=%d intervalMs=%d workMs=%d%n",
            mode, WORKERS, queueCap > 0 ? String.valueOf(queueCap) : "unbounded", tasks, intervalMs, workMs);

        ThreadPoolExecutor pool = new ThreadPoolExecutor(
            WORKERS, WORKERS, 0L, TimeUnit.MILLISECONDS, queue, policy);

        final List<Integer> doneIds   = Collections.synchronizedList(new ArrayList<Integer>());
        final List<Long>    latencies = Collections.synchronizedList(new ArrayList<Long>());
        final AtomicInteger ranByCaller = new AtomicInteger();
        int thrown = 0, maxQueue = 0;

        final long start = System.nanoTime();
        for (int i = 0; i < tasks; i++) {
            final int id = i;
            final long submitted = System.nanoTime();
            Runnable job = () -> {                                   // ① 일 하나
                if (Thread.currentThread().getName().equals("main")) ranByCaller.incrementAndGet();
                try { Thread.sleep(workMs); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                latencies.add((System.nanoTime() - submitted) / 1_000_000);   // 넣은 순간 ~ 끝난 순간
                doneIds.add(id);
            };
            try {
                pool.execute(job);                                   // ② 넣기
            } catch (RejectedExecutionException e) {
                thrown++;                                            // abort 만 여기로 온다
            }
            maxQueue = Math.max(maxQueue, pool.getQueue().size());
            Thread.sleep(intervalMs);                                // ③ 다음 일까지 간격
        }
        long submitMs = (System.nanoTime() - start) / 1_000_000;    // 넣는 쪽이 다 넣기까지 걸린 시간

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);
        long totalMs = (System.nanoTime() - start) / 1_000_000;

        int done = doneIds.size();
        long maxLat = 0, sumLat = 0;
        synchronized (latencies) { for (long l : latencies) { maxLat = Math.max(maxLat, l); sumLat += l; } }
        List<Integer> sorted;
        synchronized (doneIds) { sorted = new ArrayList<Integer>(doneIds); }
        Collections.sort(sorted);

        System.out.printf("[%-7s] done=%d thrown=%d silentlyLost=%d ranByCaller=%d maxQueue=%d "
                + "submit=%dms total=%dms avgLatency=%dms maxLatency=%dms%n",
            mode, done, thrown, tasks - done - thrown, ranByCaller.get(), maxQueue,
            submitMs, totalMs, done == 0 ? 0 : sumLat / done, maxLat);
        System.out.println("          doneIds=" + sorted);
    }

    static void printEnv() {
        System.out.printf("JDK=%s (%s), OS=%s %s, cores=%d, JVM args=%s%n",
            System.getProperty("java.version"), System.getProperty("java.vendor"),
            System.getProperty("os.name"), System.getProperty("os.version"),
            Runtime.getRuntime().availableProcessors(),
            ManagementFactory.getRuntimeMXBean().getInputArguments());
    }
}
