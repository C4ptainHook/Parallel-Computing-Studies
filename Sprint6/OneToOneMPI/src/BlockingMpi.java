import mpi.*;
import java.util.Arrays;

import mpi.*;
import java.util.Arrays;
import java.util.Random;

public class BlockingMpi {

    static final int MATRIX_A_ROWS = 2500;
    static final int MATRIX_A_COLS = 2500;
    static final int MATRIX_B_COLS = 2500;

    static final int MASTER_RANK = 0;
    static final int MSG_TAG_WORK = 10;
    static final int MSG_TAG_DONE = 20;

    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int numProcesses = MPI.COMM_WORLD.Size();
        int numWorkers = numProcesses - 1;

        double[][] matA = null;
        double[][] matB = null;
        double[][] resultMat = new double[MATRIX_A_ROWS][MATRIX_B_COLS];

        long startTime = 0;

        if (rank == MASTER_RANK) {
            System.out.println("MPI Matrix Multiplication Started with " + numProcesses + " processes.");
            if (numWorkers <= 0) {
                System.err.println("Error: Need at least one worker process.");
                MPI.Finalize();
                return;
            }

            matA = new double[MATRIX_A_ROWS][MATRIX_A_COLS];
            matB = new double[MATRIX_A_COLS][MATRIX_B_COLS];

            initializeData(matA, matB);

            startTime = System.currentTimeMillis();

            sendWorkToWorkers(numWorkers, matA, matB);

            gatherResultsFromWorkers(numWorkers, resultMat);

            long endTime = System.currentTimeMillis();
            double elapsedSeconds = (endTime - startTime) / 1000.0;

            System.out.printf("Parallel computation finished in %.3f seconds.\n", elapsedSeconds);

            System.out.println("Validating computed result...");
            if (checkResultCorrectness(matA, matB, resultMat)) {
                System.out.println("Result validation successful!");
            } else {
                System.err.println("Error: Result validation failed!");
            }

        }
        else {
            performWorkerComputation();
        }

        MPI.Finalize();
    }

    private static void initializeData(double[][] inputA, double[][] inputB) {
        for (int i = 0; i < inputA.length; i++) {
            Arrays.fill(inputA[i], 1.0);
        }
        for (int i = 0; i < inputB.length; i++) {
            Arrays.fill(inputB[i], 2.0);
        }
        System.out.println("Matrices initialized with sample data.");
    }

    private static void sendWorkToWorkers(int numWorkers, double[][] matA, double[][] matB) throws MPIException {
        System.out.println("Master distributing tasks...");
        int baseRowsPerWorker = MATRIX_A_ROWS / numWorkers;
        int remainderRows = MATRIX_A_ROWS % numWorkers;
        int currentRow = 0;

        int[] rowInfo = new int[2];

        for (int workerRank = 1; workerRank <= numWorkers; workerRank++) {
            int numRowsForWorker = (workerRank <= remainderRows) ? baseRowsPerWorker + 1 : baseRowsPerWorker;

            if (numRowsForWorker > 0) {
                rowInfo[0] = currentRow;
                rowInfo[1] = numRowsForWorker;

                MPI.COMM_WORLD.Send(rowInfo, 0, 2, MPI.INT, workerRank, MSG_TAG_WORK);
                MPI.COMM_WORLD.Send(matA, currentRow, numRowsForWorker, MPI.OBJECT, workerRank, MSG_TAG_WORK);
                MPI.COMM_WORLD.Send(matB, 0, MATRIX_A_COLS, MPI.OBJECT, workerRank, MSG_TAG_WORK);

                System.out.printf("  Sent %d rows (starting from %d) of A and full B to worker %d\n", numRowsForWorker, currentRow, workerRank);
                currentRow += numRowsForWorker;
            }
        }
        System.out.println("Task distribution complete.");
    }

    private static void gatherResultsFromWorkers(int numWorkers, double[][] resultMat) throws MPIException {
        System.out.println("Master gathering results...");
        int[] rowInfo = new int[2];
        Status status;

        for (int i = 1; i <= numWorkers; i++) {
            status = MPI.COMM_WORLD.Recv(rowInfo, 0, 2, MPI.INT, i, MSG_TAG_DONE);
            int startRow = rowInfo[0];
            int numRowsReceived = rowInfo[1];

            if (numRowsReceived > 0) {
                status = MPI.COMM_WORLD.Recv(resultMat, startRow, numRowsReceived, MPI.OBJECT, i, MSG_TAG_DONE);
                System.out.printf("  Received %d result rows (starting %d) from worker %d\n", numRowsReceived, startRow, i);
            } else {
                System.out.printf("  Worker %d reported completion with 0 rows.\n", i);
            }
        }
        System.out.println("Result gathering complete.");
    }

    private static void performWorkerComputation() throws MPIException {
        int[] rowInfo = new int[2];

        MPI.COMM_WORLD.Recv(rowInfo, 0, 2, MPI.INT, MASTER_RANK, MSG_TAG_WORK);
        int firstRowIndex = rowInfo[0];
        int numRowsToProcess = rowInfo[1];

        if (numRowsToProcess > 0) {
            double[][] workerA = new double[numRowsToProcess][MATRIX_A_COLS];
            double[][] workerB = new double[MATRIX_A_COLS][MATRIX_B_COLS];
            double[][] workerC = new double[numRowsToProcess][MATRIX_B_COLS];

            MPI.COMM_WORLD.Recv(workerA, 0, numRowsToProcess, MPI.OBJECT, MASTER_RANK, MSG_TAG_WORK);
            MPI.COMM_WORLD.Recv(workerB, 0, MATRIX_A_COLS, MPI.OBJECT, MASTER_RANK, MSG_TAG_WORK);

            computeMatrixProduct(workerA, workerB, workerC);

            MPI.COMM_WORLD.Send(rowInfo, 0, 2, MPI.INT, MASTER_RANK, MSG_TAG_DONE);
            MPI.COMM_WORLD.Send(workerC, 0, numRowsToProcess, MPI.OBJECT, MASTER_RANK, MSG_TAG_DONE);
        } else {
            MPI.COMM_WORLD.Send(rowInfo, 0, 2, MPI.INT, MASTER_RANK, MSG_TAG_DONE);
        }
    }

    private static void computeMatrixProduct(double[][] matrixA, double[][] matrixB, double[][] resultC) {
        int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int colsB = matrixB[0].length;

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                double sum = 0.0;
                for (int k = 0; k < colsA; k++) {
                    sum += matrixA[i][k] * matrixB[k][j];
                }
                resultC[i][j] = sum;
            }
        }
    }

    private static boolean checkResultCorrectness(double[][] matA, double[][] matB, double[][] parallelResultMat) {
        if (matA == null || matB == null) {
            System.err.println("Validation skipped: Input matrices not available (likely not master or error occurred).");
            return false;
        }

        int rows = matA.length;
        int cols = matB[0].length;
        double[][] serialResultMat = new double[rows][cols];

        System.out.println("Performing serial multiplication for validation...");
        computeMatrixProduct(matA, matB, serialResultMat);
        System.out.println("Serial multiplication complete.");

        double tolerance = 1e-9;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (Math.abs(parallelResultMat[i][j] - serialResultMat[i][j]) > tolerance) {
                    System.err.printf("Validation Failed at [%d][%d]: Parallel=%.5f, Serial=%.5f\n",
                            i, j, parallelResultMat[i][j], serialResultMat[i][j]);
                    return false;
                }
            }
        }
        return true;
    }
}
