package locking;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Test driver for Java 5's standard re-entrant read/write lock implementation.
 *  
 * @author Oscar Stigter
 */
public class TestStandardLocks {

    private static final int THREAD_COUNT = 100;
    private static final int ITERATION_COUNT = 10000;

    public static void main(String[] args) throws Exception {
        System.out.println("Running...");

        long time = System.currentTimeMillis();

        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        Thread[] threads = new Runner[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Runner(lock);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        long duration = System.currentTimeMillis() - time;
        System.out.printf("Finished in %d ms.", duration);
    }

    static class Runner extends Thread {

        private final ReentrantReadWriteLock lock;

        public Runner(ReentrantReadWriteLock lock) {
            this.lock = lock;
        }

        public void run() {
            for (int i = 0; i < ITERATION_COUNT; i++) {
                lock.readLock().lock();
                lock.readLock().unlock();
                lock.writeLock().lock();
                lock.writeLock().unlock();
            }
        }

    } // Runner

} // Main
