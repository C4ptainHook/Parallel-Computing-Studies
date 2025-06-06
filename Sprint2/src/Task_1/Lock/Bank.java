package Task_1.Lock;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Bank {
    public static final int NTEST = 100000;
    private final int[] accounts;
    private long ntransacts = 0;
    private final Lock lock;

    public Bank(int n, int initialBalance){
        lock = new ReentrantLock();
        accounts = new int[n];
        Arrays.fill(accounts, initialBalance);
        ntransacts = 0;
    }
    public void transfer(int from, int to, int amount) {
        try {
            lock.lock();
            accounts[from] -= amount;
            accounts[to] += amount;
            ntransacts++;
            if (ntransacts % NTEST == 0)
                test();
        }
        finally {
            lock.unlock();
        }
    }
    public void test(){
        int sum = 0;
        for(int i = 0; i < accounts.length; i++)
            sum += accounts[i] ;
        System.out.println("Transactions:" + ntransacts
                + " Sum: " + sum);
    }
    public int size(){
        return accounts.length;
    }
}
