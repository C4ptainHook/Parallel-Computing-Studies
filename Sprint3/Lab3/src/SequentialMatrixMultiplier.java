public class SequentialMatrixMultiplier implements MultiplicationAlgorithm {
    @Override
    public Matrix multiply(Matrix a, Matrix b) {
        if (a.getCols() != b.getRows()) {
            throw new IllegalArgumentException("Incorrect sizes of matrices!");
        }

        Matrix result = new Matrix(a.getRows(), b.getCols());
        for (int i = 0; i < a.getRows(); i++) {
            for (int j = 0; j < b.getCols(); j++) {
                int sum = 0;
                for (int k = 0; k < a.getCols(); k++) {
                    sum += a.get(i, k) * b.get(k, j);
                }
                result.set(i, j, sum);
            }
        }
        return result;
    }
}
