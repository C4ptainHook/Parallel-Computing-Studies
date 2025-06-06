import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class StripeMatrixMultiplier implements MultiplicationAlgorithm {
    private final int nThreads;
    private final ExecutorService pool;

    public StripeMatrixMultiplier(ExecutorService pool, int nThreads) {
        this.pool = pool;
        this.nThreads = nThreads;
    }
    @Override
    public Matrix multiply(Matrix a, Matrix b) throws InterruptedException, ExecutionException {
        if (a.getCols() != b.getRows()) {
            throw new IllegalArgumentException("Matrices dont have equal size!");
        }

        List<Future<Matrix>> futures = new ArrayList<>();
        int rowsInStripe = a.getRows() / nThreads;

        for (int i = 0; i < nThreads; i++) {
            var newProcess = new StripeMultiplicationProcess(a, i * rowsInStripe, (i + 1) * rowsInStripe, b);
            futures.addLast(pool.submit(newProcess));
        }

        for (Future<Matrix> future : futures) {
            future.get();
        }

        Matrix c = new Matrix(a.getRows(), b.getCols());
        for (int i = 0; i < a.getRows(); i++) {
            c.setRow(i, futures.get(i / rowsInStripe).get().getRow(i % rowsInStripe));
        }
        return c;
    }
}
