package Task_1.Lock;

public class TransferRunner implements Runnable {
    private final Bank bank;
    private final int fromAccount;
    private final int maxAmount;
    private static final int REPS = 1000;
    public TransferRunner(Bank b, int from, int max){
        bank = b;
        fromAccount = from;
        maxAmount = max;
    }

    public void run(){
        while (true) {
            for (int i = 0; i < REPS; i++) {
                int toAccount = (int) (bank.size() * Math.random());
                int amount = (int) (maxAmount * Math.random()/REPS);
                bank.transfer(fromAccount, toAccount, amount);
            }
        }
    }
}
