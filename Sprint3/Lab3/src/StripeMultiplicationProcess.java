import java.util.concurrent.Callable;

public class StripeMultiplicationProcess implements Callable<Matrix> {

    private final Matrix a;
    private final int aRowStart;
    private final int aRowFinish;
    private final Matrix b;

    public StripeMultiplicationProcess(Matrix a, int aRowStart, int aRowFinish, Matrix b) {
        this.a = a;
        this.aRowStart = aRowStart;
        this.aRowFinish = aRowFinish;
        if (a.getCols() != b.getRows()) {
            throw new IllegalArgumentException("Incorrect sizes of matrices!");
        }
        this.b = b;
    }

    @Override
    public Matrix call() {
        var result = new Matrix(aRowFinish - aRowStart, a.getCols());
        for(int row = aRowStart; row < aRowFinish; row++){
            for (int i = 0; i < a.getCols(); i++) {
                for (int j = 0; j < b.getCols(); j++) {
                    result.addAndSet(row - aRowStart, j, a.get(row, i) * b.get(i, j));
                }
            }
        }
        return result;
    }
}
