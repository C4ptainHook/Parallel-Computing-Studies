package com.example.matrixmultiplicationapplication.service;

import com.example.matrixmultiplicationapplication.model.IntegerMatrix;

public class StandardMatrixMultiplier implements MatrixCalculationService {

    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses a standard triple-loop algorithm.
     * It performs checks for matrix dimension compatibility and null inputs before proceeding.
     * The multiplication is C[i][j] = sum(A[i][k] * B[k][j] for k).
     * </p>
     */
    @Override
    public IntegerMatrix times(IntegerMatrix leftOperand, IntegerMatrix rightOperand) {
        if (leftOperand == null || rightOperand == null) {
            throw new IllegalArgumentException("Input matrices (leftOperand, rightOperand) cannot be null.");
        }

        int leftRows = leftOperand.getRowCount();
        int leftCols = leftOperand.getColumnCount();
        int rightRows = rightOperand.getRowCount();
        int rightCols = rightOperand.getColumnCount();

        if (leftCols != rightRows) {
            throw new IllegalArgumentException(
                    String.format(
                            "Matrices are not conformable for multiplication: " +
                                    "left operand columns (%d) must equal right operand rows (%d).",
                            leftCols, rightRows
                    )
            );
        }

        IntegerMatrix resultMatrix = new IntegerMatrix(leftRows, rightCols);
        int commonDimension = leftCols;

        for (int r = 0; r < leftRows; r++) {
            for (int c = 0; c < rightCols; c++) {
                int elementValue = calculateElementValue(leftOperand, rightOperand, r, c, commonDimension);
                resultMatrix.setElement(r, c, elementValue);
            }
        }
        return resultMatrix;
    }

    /**
     * Calculates the value for a single element in the product matrix.
     * This is effectively the dot product of a row from the {@code m1} (left matrix)
     * and a column from {@code m2} (right matrix).
     *
     * @param m1 The left-hand matrix operand.
     * @param m2 The right-hand matrix operand.
     * @param targetRow The row index in {@code m1} and the target product matrix.
     * @param targetCol The column index in {@code m2} and the target product matrix.
     * @param commonDim The common dimension (columns in m1 / rows in m2) over which to sum.
     * @return The calculated integer value for the element at (targetRow, targetCol) in the product matrix.
     */
    private int calculateElementValue(IntegerMatrix m1, IntegerMatrix m2, int targetRow, int targetCol, int commonDim) {
        int sumOfProducts = 0;
        for (int k = 0; k < commonDim; k++) {
            sumOfProducts += m1.getElement(targetRow, k) * m2.getElement(k, targetCol);
        }
        return sumOfProducts;
    }
}