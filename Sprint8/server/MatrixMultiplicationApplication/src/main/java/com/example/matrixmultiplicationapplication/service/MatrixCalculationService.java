package com.example.matrixmultiplicationapplication.service;

import com.example.matrixmultiplicationapplication.model.IntegerMatrix;

public interface MatrixCalculationService {
    /**
     * Calculates the result of multiplying matrixM1 by matrixM2.
     *
     * @param matrixM1 The first matrix in the multiplication sequence.
     * @param matrixM2 The second matrix in the multiplication sequence.
     * @return A new Matrix, the result of M1 * M2.
     * @throws IllegalArgumentException if matrix dimensions are not conformant for multiplication.
     */
    IntegerMatrix times(IntegerMatrix matrixM1, IntegerMatrix matrixM2);
}