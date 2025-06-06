package com.example.matrixmultiplicationapplication.service;

import com.example.matrixmultiplicationapplication.model.IntegerMatrix;

import java.util.ArrayList;
import java.util.List;

public class StripedParallelMultiplier implements MatrixCalculationService {
    private final int maxSubdivisionsPerDimension;

    /**
     * Constructs a StripedParallelMultiplier.
     *
     * @param maxSubdivisionsPerDimension A hint for the maximum number of subdivisions (stripes)
     *                                    to create along each dimension of the matrices.
     *                                    The actual number of threads will be up to
     *                                    {@code maxSubdivisionsPerDimension * maxSubdivisionsPerDimension}.
     *                                    Must be positive.
     */
    public StripedParallelMultiplier(int maxSubdivisionsPerDimension) {
        if (maxSubdivisionsPerDimension <= 0) {
            throw new IllegalArgumentException("Maximum subdivisions per dimension must be positive.");
        }
        this.maxSubdivisionsPerDimension = maxSubdivisionsPerDimension;
    }

    @Override
    public IntegerMatrix times(IntegerMatrix matrixA, IntegerMatrix matrixB) {
        if (matrixA == null || matrixB == null) {
            throw new IllegalArgumentException("Input matrices cannot be null.");
        }
        if (matrixA.getColumnCount() != matrixB.getRowCount()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Matrices are not conformable for multiplication: " +
                                    "matrixA columns (%d) must equal matrixB rows (%d).",
                            matrixA.getColumnCount(), matrixB.getRowCount()
                    )
            );
        }

        int rowsInA = matrixA.getRowCount();
        int colsInB = matrixB.getColumnCount();
        int numRowBlocks = Math.min(this.maxSubdivisionsPerDimension, rowsInA);
        int numColBlocks = Math.min(this.maxSubdivisionsPerDimension, colsInB);

        MatrixBlockMultiplierThread[][] workerThreads = new MatrixBlockMultiplierThread[numRowBlocks][numColBlocks];
        IntegerMatrix finalResultMatrix = new IntegerMatrix(rowsInA, colsInB);
        int baseRowsPerBlockA = rowsInA / numRowBlocks;
        int remainingRowsA = rowsInA % numRowBlocks;
        int baseColsPerBlockB = colsInB / numColBlocks;
        int remainingColsB = colsInB % numColBlocks;

        int currentMatrixARowOffset = 0;
        List<Thread> allThreads = new ArrayList<>();

        for (int i = 0; i < numRowBlocks; i++) {
            int rowsInThisBlockA = baseRowsPerBlockA + (i < remainingRowsA ? 1 : 0);
            if (rowsInThisBlockA == 0) continue;

            IntegerMatrix horizontalStripeA = matrixA.extractRowStripe(currentMatrixARowOffset, rowsInThisBlockA);
            int currentMatrixBColOffset = 0;

            for (int j = 0; j < numColBlocks; j++) {
                int colsInThisBlockB = baseColsPerBlockB + (j < remainingColsB ? 1 : 0);
                if (colsInThisBlockB == 0) continue;

                IntegerMatrix verticalStripeB = matrixB.extractColumnStripe(currentMatrixBColOffset, colsInThisBlockB);

                workerThreads[i][j] = new MatrixBlockMultiplierThread(horizontalStripeA, verticalStripeB);
                allThreads.add(workerThreads[i][j]);
                workerThreads[i][j].start();

                currentMatrixBColOffset += colsInThisBlockB;
            }
            currentMatrixARowOffset += rowsInThisBlockA;
        }

        try {
            for (Thread t : allThreads) {
                t.join();
            }
        } catch (InterruptedException e) {
            System.err.println("Striped parallel multiplication was interrupted during thread join.");
            Thread.currentThread().interrupt();
        }

        currentMatrixARowOffset = 0;
        for (int i = 0; i < numRowBlocks; i++) {
            int rowsInThisBlockA = baseRowsPerBlockA + (i < remainingRowsA ? 1 : 0);
            if (rowsInThisBlockA == 0) continue;

            int currentMatrixBColOffset = 0;
            for (int j = 0; j < numColBlocks; j++) {
                int colsInThisBlockB = baseColsPerBlockB + (j < remainingColsB ? 1 : 0);
                if (colsInThisBlockB == 0) continue;

                if (workerThreads[i][j] != null) {
                    IntegerMatrix partialProduct = workerThreads[i][j].getComputedProduct();
                    if (partialProduct != null) {
                        for (int r = 0; r < rowsInThisBlockA; r++) {
                            for (int c = 0; c < colsInThisBlockB; c++) {
                                int value = partialProduct.getElement(r, c);
                                finalResultMatrix.setElement(currentMatrixARowOffset + r, currentMatrixBColOffset + c, value);
                            }
                        }
                    } else if (Thread.currentThread().isInterrupted()){
                        System.err.println("Skipping assembly of partial product due to interruption or incomplete computation from worker " + i + "," + j);
                    }
                }
                currentMatrixBColOffset += colsInThisBlockB;
            }
            currentMatrixARowOffset += rowsInThisBlockA;
        }

        return finalResultMatrix;
    }
}