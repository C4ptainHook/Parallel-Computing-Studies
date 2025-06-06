import java.util.Random;

public class Matrix {
    private final int[][] matrix;

    public Matrix(int rows, int cols) {
        matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = 0;
            }
        }
    }

    public Matrix(int rows, int cols, int seed) {
        var random = new Random(seed);
        matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextInt();
            }
        }
    }

    public int getRows() {return matrix.length;}
    public int getCols() {return matrix[0].length;}
    public int[] getRow(int row) {return matrix[row];}
    public int get(int row, int col) {return matrix[row][col];}
    public void setRow(int index, int[] row) {
        matrix[index] = row;
    }
    public void set(int row, int col, int value) {
        matrix[row][col] = value;
    }
    public void addAndSet(int row, int col, int value) {
        matrix[row][col] += value;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        Matrix that = (Matrix) obj;
        if(getRows() != that.getRows() || getCols() != that.getCols()) return false;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] != that.matrix[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getCols(); j++) {
                sb.append(matrix[i][j]);
                if (j < getCols() - 1) {
                    sb.append("\t");
                }
            }
            if (i < getRows() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
