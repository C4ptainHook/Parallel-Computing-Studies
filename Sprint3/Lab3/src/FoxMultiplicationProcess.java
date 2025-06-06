import java.util.concurrent.Callable;

public class FoxMultiplicationProcess implements Callable<Matrix> {
    private final Matrix a;
    private final Matrix b;
    public final int i;
    public final int j;
    private final int blockSize;
    private final Matrix result;

    public FoxMultiplicationProcess(Matrix a, Matrix b, int i, int j, int blockSize) {
        if (a.getRows() != a.getCols() ||
                b.getRows() != b.getCols() ||
                a.getCols() != b.getRows())
        {
            throw new IllegalArgumentException("Incorrect sizes of matrices!");
        }
        this.a = a;
        this.b = b;
        this.i = i;
        this.j = j;
        this.blockSize = blockSize;
        result = new Matrix(blockSize, blockSize);
    }

    @Override
    public Matrix call() {
        int q = a.getRows() / blockSize;
        for (int iter = 0; iter < q; iter++) {
            multiplyAndAccumulateBlocks(a, i, (i + iter) % q,
                                        b, (i + iter) % q, j);
        }
        return result;
    }

    private void multiplyAndAccumulateBlocks(
            Matrix a, int rowBlockA, int colBlockA,
            Matrix b, int rowBlockB, int colBlockB)
    {
        for (int row = 0; row < blockSize; row++) {
            for (int col = 0; col < blockSize; col++) {
                int sum = 0;
                for (int k = 0; k < blockSize; k++) {
                    sum +=
                        a.get(rowBlockA * blockSize + row,colBlockA * blockSize + k) *
                        b.get(rowBlockB * blockSize + k,colBlockB * blockSize + col);
                }
                result.addAndSet(row, col, sum);
            }
        }
    }
}
