import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FoxMatrixMultiplier implements MultiplicationAlgorithm {
    private final int nThreads;
    private final ExecutorService pool;

    public FoxMatrixMultiplier(ExecutorService pool, int nThreads) {
        this.pool = pool;
        this.nThreads = nThreads;
    }

    @Override
    public Matrix multiply(Matrix a, Matrix b) throws ExecutionException, InterruptedException {
        if (a.getRows() != a.getCols() ||
            b.getRows() != b.getCols() ||
            a.getCols() != b.getRows())
        {
            throw new IllegalArgumentException("Incorrect sizes of matrices!");
        }

        int q = (int) Math.sqrt(nThreads);
        int blockSize = a.getRows() / q;

        List<Future<Matrix>> futures = new ArrayList<>();

        for (int i = 0; i < nThreads; i++) {
            var newProcess = new FoxMultiplicationProcess(a, b, i / q, i % q, blockSize);
            futures.addLast(pool.submit(newProcess));
        }

        for (Future<Matrix> future : futures) {
            future.get();
        }

        Matrix result = new Matrix(a.getRows(), a.getCols());
        for (int i = 0; i < futures.size(); i++) {
            for (int row = 0; row < blockSize; row++) {
                for (int col = 0; col < blockSize; col++){
                    result.set(i / q * blockSize + row, i % q * blockSize + col,
                            futures.get(i).get().get(row, col));
                }
            }
        }
        return result;
    }
}


