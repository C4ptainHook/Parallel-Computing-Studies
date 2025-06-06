import java.util.concurrent.ExecutionException;

public interface MultiplicationAlgorithm {
    Matrix multiply(Matrix a, Matrix b) throws InterruptedException, ExecutionException;
}
