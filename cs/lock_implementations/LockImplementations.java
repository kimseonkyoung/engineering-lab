import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 락 구현 방식별 정확성과 속도 비교.
 * 두 스레드가 각자 N번, 락으로 보호된 balance = balance + 1을 실행한다.
 *
 *   naive     : volatile 깃발, 확인과 설정이 분리됨 (틈 있음)
 *   spin      : AtomicInteger.compareAndSet으로 확인과 설정을 원자 명령 하나로
 *   reentrant : ReentrantLock (비공정, 기본값)
 *   fair      : ReentrantLock(true) (공정 모드)
 *   sync      : synchronized
 */
public class LockImplementations {
    static final int N = 1_000_000;
    static final int ROUNDS = 5;
    static int balance;

    interface MyLock { void lock(); void unlock(); }

    static class NaiveLock implements MyLock {
        private volatile int state = 0;
        public void lock()   { while (state == 1) { } state = 1; }
        public void unlock() { state = 0; }
    }

    static class SpinLock implements MyLock {
        private final AtomicInteger state = new AtomicInteger(0);
        public void lock()   { while (!state.compareAndSet(0, 1)) { } }
        public void unlock() { state.set(0); }
    }

    static class Reentrant implements MyLock {
        private final ReentrantLock inner;
        Reentrant(boolean fair) { inner = new ReentrantLock(fair); }
        public void lock()   { inner.lock(); }
        public void unlock() { inner.unlock(); }
    }

    static final Object MONITOR = new Object();

    public static void main(String[] args) throws InterruptedException {
        printEnv();
        for (int round = 1; round <= ROUNDS; round++) {
            System.out.println("--- round " + round);
            run("naive",     new NaiveLock());
            run("spin",      new SpinLock());
            run("reentrant", new Reentrant(false));
            run("fair",      new Reentrant(true));
            runSync();
        }
    }

    static void run(String name, MyLock lock) throws InterruptedException {
        Runnable task = () -> {
            for (int i = 0; i < N; i++) {
                lock.lock();
                try { balance = balance + 1; } finally { lock.unlock(); }
            }
        };
        measure(name, task);
    }

    static void runSync() throws InterruptedException {
        Runnable task = () -> {
            for (int i = 0; i < N; i++) {
                synchronized (MONITOR) { balance = balance + 1; }
            }
        };
        measure("sync", task);
    }

    static void measure(String name, Runnable task) throws InterruptedException {
        balance = 0;
        Thread t1 = new Thread(task), t2 = new Thread(task);
        long start = System.nanoTime();
        t1.start(); t2.start();
        t1.join();  t2.join();
        System.out.printf("[%-9s] balance=%,d lost=%,d time=%dms%n",
            name, balance, 2 * N - balance, (System.nanoTime() - start) / 1_000_000);
    }

    static void printEnv() {
        System.out.printf("JDK=%s (%s), OS=%s %s, cores=%d, JVM args=%s%n",
            System.getProperty("java.version"), System.getProperty("java.vendor"),
            System.getProperty("os.name"), System.getProperty("os.version"),
            Runtime.getRuntime().availableProcessors(),
            ManagementFactory.getRuntimeMXBean().getInputArguments());
    }
}
