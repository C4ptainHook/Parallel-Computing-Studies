package Task_1.BaseImplementation;

import java.util.Arrays;

public class Bank {
    public static final int NTEST = 100000;
    private final int[] accounts;
    private long ntransacts = 0;
    public Bank(int n, int initialBalance){
        accounts = new int[n];
        Arrays.fill(accounts, initialBalance);
        ntransacts = 0;
    }
    public void transfer(int from, int to, int amount) {
        accounts[from] -= amount;
        accounts[to] += amount;
        ntransacts++;
        if (ntransacts % NTEST == 0)
            test();
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
