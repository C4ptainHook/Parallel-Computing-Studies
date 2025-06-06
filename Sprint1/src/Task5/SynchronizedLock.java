package Task5;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedLock implements Synchronizable {
    private int x = 0;
    private final Lock lock = new ReentrantLock();

    public void increment() {

        for(int i = 0; i < 100000; i++){
            try {
                lock.lock();
                x++;
            }
            finally {
                lock.unlock();
            }
        }
    }

    public void decrement() {
        for(int i = 0; i < 100000; i++){
            try {
                lock.lock();
                x--;
            }
            finally {
                lock.unlock();
            }
        }
    }

    public int getX() {
        return x;
    }
}
