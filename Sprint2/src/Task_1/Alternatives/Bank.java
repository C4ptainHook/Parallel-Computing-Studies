package Task_1.Alternatives;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Bank {
    public static final int NUMBEROFTESTS = 1000000;
    private final int[] accounts;
    private final AtomicLong numberOfTransacts;
    private final Lock lock = new ReentrantLock();

    public Bank(int n, int initialBalance){
        accounts = new int[n];
        Arrays.fill(accounts, initialBalance);
        numberOfTransacts = new AtomicLong(0);
    }
    public void transfer(int from, int to, int amount) {
        try {
            lock.lock();
            accounts[from] -= amount;
            accounts[to] += amount;
        }
        finally {
            lock.unlock();
        }
        numberOfTransacts.incrementAndGet();
        if (numberOfTransacts.get() % NUMBEROFTESTS == 0) {
            test();
        }
    }
    public void test(){
        try {
            lock.lock();
            int sum = 0;
            for (int account : accounts) sum += account;
            System.out.println("Transactions:" + numberOfTransacts.get()
                    + " Sum: " + sum);
        }
        finally{
            lock.unlock();
        }
    }
    public int size(){
        return accounts.length;
    }
}
