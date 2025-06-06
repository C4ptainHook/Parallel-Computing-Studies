package com.example.matrixmultiplicationapplication.service;

import com.example.matrixmultiplicationapplication.model.IntegerMatrix;

public class MatrixBlockMultiplierThread extends Thread {

    private final IntegerMatrix matrixBlockA;
    private final IntegerMatrix matrixBlockB;
    private IntegerMatrix partialProduct;

    /**
     * Constructs a new task to multiply two matrix blocks.
     *
     * @param blockA The first matrix block (e.g., a horizontal stripe or a general sub-matrix).
     *               Must not be null. Its column count must match {@code blockB}'s row count.
     * @param blockB The second matrix block (e.g., a vertical stripe or a general sub-matrix).
     *               Must not be null. Its row count must match {@code blockA}'s column count.
     * @throws IllegalArgumentException if blocks are null or their dimensions are incompatible.
     */
    public MatrixBlockMultiplierThread(IntegerMatrix blockA, IntegerMatrix blockB) {
        if (blockA == null || blockB == null) {
            throw new IllegalArgumentException("Input matrix blocks cannot be null.");
        }
        if (blockA.getColumnCount() != blockB.getRowCount()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Incompatible dimensions for block multiplication: blockA columns (%d) != blockB rows (%d)",
                            blockA.getColumnCount(), blockB.getRowCount()
                    )
            );
        }
        this.matrixBlockA = blockA;
        this.matrixBlockB = blockB;
    }

    /**
     * Executes the matrix block multiplication.
     * The result is stored internally and can be retrieved using {@link #getComputedProduct()}.
     */
    @Override
    public void run() {
        int resultRows = matrixBlockA.getRowCount();
        int resultCols = matrixBlockB.getColumnCount();
        int commonDim = matrixBlockA.getColumnCount();
        this.partialProduct = new IntegerMatrix(resultRows, resultCols);

        for (int r = 0; r < resultRows; r++) {
            for (int c = 0; c < resultCols; c++) {
                int sumOfProducts = 0;
                for (int k = 0; k < commonDim; k++) {
                    sumOfProducts += matrixBlockA.getElement(r, k) * matrixBlockB.getElement(k, c);
                }
                this.partialProduct.setElement(r, c, sumOfProducts);
            }
        }
    }

    /**
     * Retrieves the resulting matrix product computed by this thread.
     * This method should be called after the thread has completed its execution (e.g., after {@code join()}).
     *
     * @return The {@link IntegerMatrix} representing the product of the input blocks.
     *         Returns null if the thread has not yet computed the product (i.e., {@code run()} hasn't finished).
     *         Consider throwing IllegalStateException if called before completion for stricter contract.
     */
    public IntegerMatrix getComputedProduct() {
        return this.partialProduct;
    }
}