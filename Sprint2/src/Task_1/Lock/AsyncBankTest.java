package Task_1.Lock;

public class AsyncBankTest {
    public static final int NACCOUNTS = 10;
    public static final int INITIAL_BALANCE = 10000;
    public static void main(String[] args) {
        Bank b = new Bank(NACCOUNTS, INITIAL_BALANCE);

        for (int i = 0; i < NACCOUNTS; i++){
            var newThread = new Thread(new TransferRunner(b, i, INITIAL_BALANCE));
            newThread.setPriority(Thread.NORM_PRIORITY + i % 2);
            newThread.start();
        }
    }
}
