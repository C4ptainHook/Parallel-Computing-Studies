package com.example.matrixmultiplicationapplication.model;

import java.util.Arrays;
import java.util.Random;

public final class IntegerMatrix {
    private final int[][] elements;
    private final int rowCount;
    private final int columnCount;

    /**
     * Constructs a new IntegerMatrix with the specified number of rows and columns.
     * All elements are initialized to zero.
     *
     * @param numRows The number of rows in the matrix. Must be positive.
     * @param numCols The number of columns in the matrix. Must be positive.
     * @throws IllegalArgumentException if numRows or numCols is not positive.
     */
    public IntegerMatrix(int numRows, int numCols) {
        if (numRows <= 0 || numCols <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be positive: " + numRows + "x" + numCols);
        }
        this.rowCount = numRows;
        this.columnCount = numCols;
        this.elements = new int[numRows][numCols];
    }

    /**
     * Constructs a new IntegerMatrix with specified dimensions and populates it
     * with random integer values using the given seed.
     * Values will be in the range [0, 23].
     *
     * @param numRows The number of rows. Must be positive.
     * @param numCols The number of columns. Must be positive.
     * @param seed    The seed for the random number generator.
     * @throws IllegalArgumentException if numRows or numCols is not positive.
     */
    public IntegerMatrix(int numRows, int numCols, int seed) {
        if (numRows <= 0 || numCols <= 0) {
            throw new IllegalArgumentException("Matrix dimensions must be positive: " + numRows + "x" + numCols);
        }
        this.rowCount = numRows;
        this.columnCount = numCols;
        this.elements = new int[numRows][numCols];
        Random randomGenerator = new Random(seed);
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                this.elements[r][c] = randomGenerator.nextInt(24);
            }
        }
    }

    /**
     * Constructs a new IntegerMatrix by performing a deep copy of the provided 2D array data.
     *
     * @param sourceData The 2D array to copy elements from. Cannot be null, empty,
     *                   or contain null rows. Must be rectangular.
     * @throws IllegalArgumentException if sourceData is null, empty, jagged, or contains null rows.
     */
    public IntegerMatrix(int[][] sourceData) {
        if (sourceData == null || sourceData.length == 0) {
            throw new IllegalArgumentException("Source data cannot be null or empty.");
        }
        if (sourceData[0] == null || sourceData[0].length == 0) {
            throw new IllegalArgumentException("Source data cannot have zero columns or null first row.");
        }

        this.rowCount = sourceData.length;
        this.columnCount = sourceData[0].length;
        this.elements = new int[this.rowCount][this.columnCount];

        for (int r = 0; r < this.rowCount; r++) {
            if (sourceData[r] == null || sourceData[r].length != this.columnCount) {
                throw new IllegalArgumentException("Source data is jagged or contains null rows. All rows must have " + this.columnCount + " columns.");
            }
            System.arraycopy(sourceData[r], 0, this.elements[r], 0, this.columnCount);
        }
    }

    /**
     * Returns the number of rows in this matrix.
     * @return The row count.
     */
    public int getRowCount() {
        return this.rowCount;
    }

    /**
     * Returns the number of columns in this matrix.
     * @return The column count.
     */
    public int getColumnCount() {
        return this.columnCount;
    }

    /**
     * Retrieves the element at the specified row and column.
     *
     * @param rowIndex    The row index (0-based).
     * @param columnIndex The column index (0-based).
     * @return The integer value at the specified position.
     * @throws IndexOutOfBoundsException if rowIndex or columnIndex is out of bounds.
     */
    public int getElement(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= this.rowCount || columnIndex < 0 || columnIndex >= this.columnCount) {
            throw new IndexOutOfBoundsException(
                    String.format("Index (%d, %d) out of bounds for matrix of size %dx%d.",
                            rowIndex, columnIndex, this.rowCount, this.columnCount)
            );
        }
        return this.elements[rowIndex][columnIndex];
    }

    /**
     * Sets the element at the specified row and column to the given value.
     *
     * @param rowIndex    The row index (0-based).
     * @param columnIndex The column index (0-based).
     * @param value       The new integer value for the element.
     * @throws IndexOutOfBoundsException if rowIndex or columnIndex is out of bounds.
     */
    public void setElement(int rowIndex, int columnIndex, int value) {
        if (rowIndex < 0 || rowIndex >= this.rowCount || columnIndex < 0 || columnIndex >= this.columnCount) {
            throw new IndexOutOfBoundsException(
                    String.format("Index (%d, %d) out of bounds for matrix of size %dx%d.",
                            rowIndex, columnIndex, this.rowCount, this.columnCount)
            );
        }
        this.elements[rowIndex][columnIndex] = value;
    }

    /**
     * Extracts a horizontal stripe (a sub-matrix composed of a contiguous block of rows)
     * from this matrix.
     *
     * @param startRowIndex The starting row index (inclusive, 0-based) of the stripe.
     * @param numberOfRows  The number of rows to include in the stripe.
     *                      If (startRowIndex + numberOfRows) exceeds matrix bounds,
     *                      the stripe will include rows up to the last row of the matrix.
     * @return A new {@code IntegerMatrix} representing the horizontal stripe.
     * @throws IllegalArgumentException if startRowIndex is out of bounds or numberOfRows is not positive.
     */
    public IntegerMatrix extractRowStripe(int startRowIndex, int numberOfRows) {
        if (startRowIndex < 0 || startRowIndex >= this.rowCount) {
            throw new IllegalArgumentException("Start row index " + startRowIndex + " is out of bounds for " + this.rowCount + " rows.");
        }
        if (numberOfRows <= 0) {
            throw new IllegalArgumentException("Number of rows must be positive.");
        }

        int actualNumRows = Math.min(numberOfRows, this.rowCount - startRowIndex);
        int[][] stripeData = new int[actualNumRows][this.columnCount];

        for (int i = 0; i < actualNumRows; i++) {
            System.arraycopy(this.elements[startRowIndex + i], 0, stripeData[i], 0, this.columnCount);
        }
        return new IntegerMatrix(stripeData);
    }

    /**
     * Extracts a vertical stripe (a sub-matrix composed of a contiguous block of columns)
     * from this matrix.
     *
     * @param startColumnIndex The starting column index (inclusive, 0-based) of the stripe.
     * @param numberOfColumns  The number of columns to include in the stripe.
     *                         If (startColumnIndex + numberOfColumns) exceeds matrix bounds,
     *                         the stripe will include columns up to the last column of the matrix.
     * @return A new {@code IntegerMatrix} representing the vertical stripe.
     * @throws IllegalArgumentException if startColumnIndex is out of bounds or numberOfColumns is not positive.
     */
    public IntegerMatrix extractColumnStripe(int startColumnIndex, int numberOfColumns) {
        if (startColumnIndex < 0 || startColumnIndex >= this.columnCount) {
            throw new IllegalArgumentException("Start column index " + startColumnIndex + " is out of bounds for " + this.columnCount + " columns.");
        }
        if (numberOfColumns <= 0) {
            throw new IllegalArgumentException("Number of columns must be positive.");
        }

        int actualNumCols = Math.min(numberOfColumns, this.columnCount - startColumnIndex);
        int[][] stripeData = new int[this.rowCount][actualNumCols];

        for (int r = 0; r < this.rowCount; r++) {
            System.arraycopy(this.elements[r], startColumnIndex, stripeData[r], 0, actualNumCols);
        }
        return new IntegerMatrix(stripeData);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        IntegerMatrix other = (IntegerMatrix) obj;
        if (this.rowCount != other.rowCount || this.columnCount != other.columnCount) {
            return false;
        }
        for (int r = 0; r < this.rowCount; r++) {
            for (int c = 0; c < this.columnCount; c++) {
                if (this.elements[r][c] != other.elements[r][c]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + rowCount;
        result = 31 * result + columnCount;
        for (int r = 0; r < rowCount; r++) {
            result = 31 * result + Arrays.hashCode(elements[r]);
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IntegerMatrix (").append(rowCount).append("x").append(columnCount).append("):\n");
        for (int r = 0; r < rowCount; r++) {
            sb.append(Arrays.toString(elements[r])).append("\n");
        }
        return sb.toString();
    }
}