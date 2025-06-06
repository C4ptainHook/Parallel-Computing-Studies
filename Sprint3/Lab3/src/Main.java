import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        var pool = Executors.newCachedThreadPool();
        var matrices = new int[]{8};
        var threadNumbers = new int[]{4};

        try {
            System.out.printf("%-10s %-10s %-15s %-15s %-15s %-15s %-15s\n", "Size", "Threads", "Seq Time (s)", "Stripe Time (s)", "Stripe Speedup", "Fox Time (s)", "Fox Speedup");
            System.out.println("----------------------------------------------------------------------------------------------------------");
            for (int size : matrices) {
                for (int nThreads : threadNumbers) {
                    Experiment(size, nThreads, pool);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                    if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                        System.err.println("Pool did not terminate");
                }
            } catch (InterruptedException ie) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("Completed experiment");
        }
    }

    public static void Experiment(int matrixSize, int nThreads, ExecutorService pool)
            throws InterruptedException, ExecutionException {
        Matrix a = new Matrix(matrixSize, matrixSize, 1);
        Matrix b = new Matrix(matrixSize, matrixSize, 2);

        long sequentialStartTime = System.nanoTime();
        var sequential = new SequentialMatrixMultiplier().multiply(a, b);
        long sequentialTime = System.nanoTime() - sequentialStartTime;

        long stripeStartTime = System.nanoTime();
        var stripe = new StripeMatrixMultiplier(pool, nThreads).multiply(a, b);
        long stripeTime = System.nanoTime() - stripeStartTime;

        long foxStartTime = System.nanoTime();
        var fox = new FoxMatrixMultiplier(pool, nThreads).multiply(a, b);
        long foxTime = System.nanoTime() - foxStartTime;

        System.out.println("Matrix from sequential operations:\n " + sequential);
        System.out.println("Matrix from fox operations:\n " + fox);
        if (!sequential.equals(stripe)) {
            System.out.println("Results are inconsistent!");
            return;
        }

        double seqS = sequentialTime / 1_000_000_000.0;
        double stripeS = stripeTime / 1_000_000_000.0;
//        double foxS = foxTime / 1_000_000_000.0;
        double stripeSpeedup = seqS / stripeS;
//        double foxSpeedup = seqS / foxS;

//        System.out.printf("%-10d %-10d %-15.4f %-15.4f %-15.4f %-15.4f %-15.4f\n", matrixSize, nThreads, seqS, stripeS, stripeSpeedup, foxS, foxSpeedup);
    }
}
