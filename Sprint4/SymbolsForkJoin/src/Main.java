import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class Main {
    private static final int LINE_LENGTH = 50;
    private static final int LINES_NUMBER = 10;

    public static void main(String[] args) throws InterruptedException {
        Printer printer = new Printer(3, LINE_LENGTH);
        int totalChars = LINE_LENGTH * LINES_NUMBER / 3;

        long startThreads = System.nanoTime();
        Thread thread1 = new Thread(new PrintRunnable(printer, '|', 0, totalChars));
        Thread thread2 = new Thread(new PrintRunnable(printer, '\\', 1, totalChars));
        Thread thread3 = new Thread(new PrintRunnable(printer, '/', 2, totalChars));

        thread1.start();
        thread2.start();
        thread3.start();

        thread1.join();
        thread2.join();
        thread3.join();

        long endThreads = System.nanoTime();

        double threadsTime = (endThreads - startThreads) / 1e6;

        System.out.println("\n------------------------------");
        System.out.println("Result for classic threads " + threadsTime + " ms");
        System.out.println("------------------------------");

        printer = new Printer(3, LINE_LENGTH);

        long startFJ = System.nanoTime();
        try (ForkJoinPool pool = new ForkJoinPool()) {
            PrintTask task1 = new PrintTask(printer, '|', 0, totalChars);
            PrintTask task2 = new PrintTask(printer, '\\', 1, totalChars);
            PrintTask task3 = new PrintTask(printer, '/', 2, totalChars);

            ForkJoinTask<Void> t1 = pool.submit(task1);
            ForkJoinTask<Void> t2 = pool.submit(task2);
            ForkJoinTask<Void> t3 = pool.submit(task3);

            t1.join();
            t2.join();
            t3.join();
        }
        long endFJ = System.nanoTime();
        double forkJoinTime = (endFJ - startFJ) / 1e6;

        System.out.println("\n------------------------------");
        System.out.println("Results for ForkJoin: " + forkJoinTime + " ms");
        System.out.println("------------------------------\n");

        if (forkJoinTime > 0) {
            double speedup = threadsTime / forkJoinTime;
            System.out.printf("Speedup (Normal Parallel / ForkJoin): %.2f\n", speedup);
        }
    }
}
