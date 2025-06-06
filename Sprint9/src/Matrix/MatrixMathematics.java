package Matrix;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatrixMathematics {

	/**
	 * This class a matrix utility class and cannot be instantiated.
	 */
	private MatrixMathematics(){}
	/**
	 * Transpose of a matrix - Swap the columns with rows
	 * @param matrix
	 * @return
	 */
	public static Matrix transpose(Matrix matrix) {
		Matrix transposedMatrix = new Matrix(matrix.getNcols(), matrix.getNrows());
		for (int i=0;i<matrix.getNrows();i++) {
			for (int j=0;j<matrix.getNcols();j++) {
				transposedMatrix.setValueAt(j, i, matrix.getValueAt(i, j));
			}
		}
		return transposedMatrix;
	}
	
	/**
	 * Inverse of a matrix - A-1 * A = I where I is the identity matrix
	 * A matrix that have inverse is called non-singular or invertible. If the matrix does not have inverse it is called singular.
	 * For a singular matrix the values of the inverted matrix are either NAN or Infinity
	 * Only square matrices have inverse and the following method will throw exception if the matrix is not square.
	 * @param matrix
	 * @return
	 * @throws NoSquareException
	 */
	public static Matrix inverse(Matrix matrix) throws NoSquareException {
		if(determinant(matrix)==0)
                    throw new NoSquareException("determinant equals zero");
                
            return (transpose(cofactor(matrix)).multiplyByConstant(1.0/determinant(matrix)));
	}
	
	/**
	 * Determinant of a square matrix
	 * The following function find the determinant in a recursively. 
	 * @param matrix
	 * @return
	 * @throws NoSquareException
	 */
	public static double determinant(Matrix matrix) throws NoSquareException {
		if (!matrix.isSquare())
			throw new NoSquareException("matrix need to be square.");
		if (matrix.size() == 1){
			return matrix.getValueAt(0, 0);
		}
			
		if (matrix.size()==2) {
			return (matrix.getValueAt(0, 0) * matrix.getValueAt(1, 1)) - ( matrix.getValueAt(0, 1) * matrix.getValueAt(1, 0));
		}
		double sum = 0.0;
		for (int i=0; i<matrix.getNcols(); i++) {
			sum += changeSign(i) * matrix.getValueAt(0, i) * determinant(createSubMatrix(matrix, 0, i));
		}
		return sum;
	}

	/**
	 * Determine the sign; i.e. even numbers have sign + and odds -
	 * @param i
	 * @return
	 */
	private static int changeSign(int i) {
		if (i%2==0)
			return 1;
		return -1;
	}
	/**
	 * Creates a submatrix excluding the given row and column
	 * @param matrix
	 * @param excluding_row
	 * @param excluding_col
	 * @return
	 */
	public static Matrix createSubMatrix(Matrix matrix, int excluding_row, int excluding_col) {
		Matrix mat = new Matrix(matrix.getNrows()-1, matrix.getNcols()-1);
		int r = -1;
		for (int i=0;i<matrix.getNrows();i++) {
			if (i==excluding_row)
				continue;
				r++;
				int c = -1;
			for (int j=0;j<matrix.getNcols();j++) {
				if (j==excluding_col)
					continue;
				mat.setValueAt(r, ++c, matrix.getValueAt(i, j));
			}
		}
		return mat;
	}
	
	/**
	 * The cofactor of a matrix
	 * @param matrix
	 * @return
	 * @throws NoSquareException
	 */
	public static Matrix cofactor(Matrix matrix) throws NoSquareException {
		Matrix mat = new Matrix(matrix.getNrows(), matrix.getNcols());
		for (int i=0;i<matrix.getNrows();i++) {
			for (int j=0; j<matrix.getNcols();j++) {
				mat.setValueAt(i, j, changeSign(i) * changeSign(j) * determinant(createSubMatrix(matrix, i, j)));
			}
		}
		
		return mat;
	}
	
	/**
	 * Adds two matrices of the same dimension
	 * @param matrix1
	 * @param matrix2
	 * @return
	 * @throws IllegalDimensionException
	 */
	public static Matrix add(Matrix matrix1, Matrix matrix2) throws IllegalDimensionException {
		if (matrix1.getNcols() != matrix2.getNcols() || matrix1.getNrows() != matrix2.getNrows())
			throw new IllegalDimensionException("Two matrices should be the same dimension.");
		Matrix sumMatrix = new Matrix(matrix1.getNrows(), matrix1.getNcols());
		for (int i=0; i<matrix1.getNrows();i++) {
			for (int j=0;j<matrix1.getNcols();j++) 
				sumMatrix.setValueAt(i, j, matrix1.getValueAt(i, j) + matrix2.getValueAt(i,j));
			
		}
		return sumMatrix;
	}
	
	/**
	 * subtract two matrices using the above addition method. Matrices should be the same dimension.
	 * @param matrix1
	 * @param matrix2
	 * @return
	 * @throws IllegalDimensionException
	 */
	public static Matrix subtract(Matrix matrix1, Matrix matrix2) throws IllegalDimensionException {
		return add(matrix1,matrix2.multiplyByConstant(-1));
	}
	
	/**
	 * Multiply two matrices
	 * 
	 * @param A
	 * @param B
	 * @param numThreads
	 * @return 
	 */
	public static Matrix multiply(Matrix A, Matrix B, int numThreads) {
		numThreads = 1;
		double[][] a = A.getValues();
		double[][] b = B.getValues();
		int rowsA = a.length, colsA = a[0].length, colsB = b[0].length;
		double[][] result = new double[rowsA][colsB];

		int chunkSize = (int) Math.ceil((double) rowsA / numThreads);
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		CountDownLatch latch = new CountDownLatch(numThreads);

		for (int t = 0; t < numThreads; t++) {
			int start = t * chunkSize;
			int end = Math.min(start + chunkSize, rowsA);

			executor.submit(() -> {
				for (int i = start; i < end; i++) {
					for (int j = 0; j < colsB; j++) {
						for (int k = 0; k < colsA; k++) {
							result[i][j] += a[i][k] * b[k][j];
						}
					}
				}
				latch.countDown();
			});
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		executor.shutdown();
		return new Matrix(result);
	}
}
