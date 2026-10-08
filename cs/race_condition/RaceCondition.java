import java.lang.management.ManagementFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 두 스레드가 공유 변수를 각각 N번 증가시킬 때의 잃어버린 갱신 관찰.
 *
 * 프로그램 인자: <mode> [latch]
 *   mode  = none | volatile | lock | sync
 *   latch = 두 스레드를 CountDownLatch로 동시에 출발시킴
 * VM 인자 예: -Xint (JIT 끄기), -XX:+PrintCompilation (컴파일 시점 출력)
 */
public class RaceCondition {
    static final int N = 1_000_000;
    static final int ROUNDS = 20;
    static final ReentrantLock lock = new ReentrantLock();
    static final Object monitor = new Object();
    static int balance = 0;
    static volatile int vBalance = 0;

    public static void main(String[] args) throws InterruptedException {
        final String mode = args.length > 0 ? args[0] : "none";
        final boolean latch = args.length > 1 && "latch".equals(args[1]);
        final Runnable body = pick(mode);   // 알 수 없는 모드면 여기서 바로 실패
        printEnv();

        for (int round = 1; round <= ROUNDS; round++) {
            balance = 0;
            vBalance = 0;
            final CountDownLatch go = new CountDownLatch(1);
            Runnable task = () -> {
                if (latch) {
                    try { go.await(); } catch (InterruptedException e) { return; }
                }
                body.run();
            };
            Thread t1 = new Thread(task);
            Thread t2 = new Thread(task);
            long start = System.nanoTime();
            t1.start();
            t2.start();
            go.countDown();
            t1.join();
            t2.join();
            long ms = (System.nanoTime() - start) / 1_000_000;
            int result = "volatile".equals(mode) ? vBalance : balance;
            System.out.printf("[%s%s] round %2d balance=%,d lost=%,d time=%dms%n",
                mode, latch ? "+latch" : "", round, result, 2 * N - result, ms);
        }
    }

    static Runnable pick(String mode) {
        switch (mode) {
            case "none":     return RaceCondition::noLock;
            case "volatile": return RaceCondition::withVolatile;
            case "lock":     return RaceCondition::withLock;
            case "sync":     return RaceCondition::withSync;
            default: throw new IllegalArgumentException("unknown mode: " + mode);
        }
    }

    static void noLock()       { for (int i = 0; i < N; i++) balance = balance + 1; }
    static void withVolatile() { for (int i = 0; i < N; i++) vBalance = vBalance + 1; }

    static void withLock() {
        for (int i = 0; i < N; i++) {
            lock.lock();
            try { balance = balance + 1; } finally { lock.unlock(); }
        }
    }

    static void withSync() {
        for (int i = 0; i < N; i++) {
            synchronized (monitor) { balance = balance + 1; }
        }
    }

    static void printEnv() {
        System.out.printf("JDK=%s (%s), OS=%s %s, cores=%d, JVM args=%s%n",
            System.getProperty("java.version"), System.getProperty("java.vendor"),
            System.getProperty("os.name"), System.getProperty("os.version"),
            Runtime.getRuntime().availableProcessors(),
            ManagementFactory.getRuntimeMXBean().getInputArguments());
    }
}
