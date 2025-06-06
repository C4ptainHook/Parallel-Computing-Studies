import mpi.*;
import java.util.Arrays;

import mpi.*;
import java.util.Arrays;
import java.util.Random;

public class NonBlockingMpi {

    static final int DIM_M = 2500;
    static final int DIM_N = 2500;
    static final int DIM_P = 2500;

    static final int COORD_RANK = 0;
    static final int MSG_TAG_TASK = 11;
    static final int MSG_TAG_RESULT = 22;

    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        int myRank = MPI.COMM_WORLD.Rank();
        int clusterSize = MPI.COMM_WORLD.Size();
        int numHelpers = clusterSize - 1;

        if (numHelpers < 1) {
            if (myRank == COORD_RANK) {
                System.err.println("Error: Requires at least 2 processes (1 coordinator, 1+ helper).");
            }
            MPI.Finalize();
            System.exit(1);
        }

        double[][] matM = null;
        double[][] matN = null;
        double[][] matP = new double[DIM_M][DIM_P];

        long executionStartTime = 0;

        if (myRank == COORD_RANK) {
            matM = new double[DIM_M][DIM_N];
            matN = new double[DIM_N][DIM_P];

            setupData(matM, matN);

            executionStartTime = System.currentTimeMillis();

            dispatchWorkAsync(numHelpers, matM, matN);
            gatherResultsAsync(numHelpers, matP);

            long executionEndTime = System.currentTimeMillis();
            double timeElapsed = (executionEndTime - executionStartTime) / 1000.0;

            System.out.printf("Asynchronous computation completed in %.3f seconds.\n", timeElapsed);
            System.out.println("Verifying final result...");
            verifyComputation(matM, matN, matP);

        } else {
            processAssignedWork();
        }

        MPI.Finalize();
    }

    private static void setupData(double[][] matrixM, double[][] matrixN) {
        Random rand = new Random(123);
        for (int i = 0; i < matrixM.length; i++) {
            for (int j = 0; j < matrixM[0].length; j++) {
                matrixM[i][j] = rand.nextDouble();
            }
        }
        for (int i = 0; i < matrixN.length; i++) {
            for (int j = 0; j < matrixN[0].length; j++) {
                matrixN[i][j] = rand.nextDouble() * 2.0;
            }
        }
        System.out.println("Input matrices M and N initialized with random data.");
    }

    private static void dispatchWorkAsync(int helperCount, double[][] fullMatM, double[][] fullMatN) throws MPIException {
        System.out.println("Coordinator dispatching tasks asynchronously...");
        int baseRowsPerHelper = DIM_M / helperCount;
        int leftoverRows = DIM_M % helperCount;
        int assignedRowStart = 0;

        Request[] taskInfoHandles = new Request[helperCount];
        Request[] matMHandles = new Request[helperCount];
        Request[] matNHandles = new Request[helperCount];

        for (int targetRank = 1; targetRank <= helperCount; targetRank++) {
            int rowsForThisHelper = (targetRank <= leftoverRows) ? baseRowsPerHelper + 1 : baseRowsPerHelper;
            int helperIndex = targetRank - 1;

            if (rowsForThisHelper > 0) {
                int[] currentTaskInfo = new int[]{assignedRowStart, rowsForThisHelper};

                taskInfoHandles[helperIndex] = MPI.COMM_WORLD.Isend(currentTaskInfo, 0, 2, MPI.INT, targetRank, MSG_TAG_TASK);
                matMHandles[helperIndex] = MPI.COMM_WORLD.Isend(fullMatM, assignedRowStart, rowsForThisHelper, MPI.OBJECT, targetRank, MSG_TAG_TASK);
                matNHandles[helperIndex] = MPI.COMM_WORLD.Isend(fullMatN, 0, DIM_N, MPI.OBJECT, targetRank, MSG_TAG_TASK);

                assignedRowStart += rowsForThisHelper;
            } else {
                taskInfoHandles[helperIndex] = MPI.REQUEST_NULL;
                matMHandles[helperIndex] = MPI.REQUEST_NULL;
                matNHandles[helperIndex] = MPI.REQUEST_NULL;
            }
        }

        Request.Waitall(taskInfoHandles);
        Request.Waitall(matMHandles);
        Request.Waitall(matNHandles);

        System.out.println("All task data sent by coordinator.");
    }


    private static void gatherResultsAsync(int helperCount, double[][] finalMatP) throws MPIException {
        System.out.println("Coordinator gathering results asynchronously...");
        int[][] receivedTaskInfo = new int[helperCount][2];
        Request[] resultInfoHandles = new Request[helperCount];
        Request[] resultDataHandles = new Request[helperCount];

        for (int i = 0; i < helperCount; i++) {
            resultInfoHandles[i] = MPI.COMM_WORLD.Irecv(receivedTaskInfo[i], 0, 2, MPI.INT, i + 1, MSG_TAG_RESULT);
        }

        Request.Waitall(resultInfoHandles);
        System.out.println("Received all result metadata from helpers.");

        for (int i = 0; i < helperCount; i++) {
            int resultStartRow = receivedTaskInfo[i][0];
            int numResultRows = receivedTaskInfo[i][1];

            if (numResultRows > 0) {
                resultDataHandles[i] = MPI.COMM_WORLD.Irecv(finalMatP, resultStartRow, numResultRows, MPI.OBJECT, i + 1, MSG_TAG_RESULT);
            } else {
                resultDataHandles[i] = MPI.REQUEST_NULL;
            }
        }

        Request.Waitall(resultDataHandles);
        System.out.println("All computed result blocks received by coordinator.");
    }

    private static void processAssignedWork() throws MPIException {
        int[] taskInfo = new int[2];
        Request taskInfoHandle = MPI.COMM_WORLD.Irecv(taskInfo, 0, 2, MPI.INT, COORD_RANK, MSG_TAG_TASK);
        taskInfoHandle.Wait();

        int myStartRow = taskInfo[0];
        int myRowCount = taskInfo[1];

        if (myRowCount <= 0) {
            MPI.COMM_WORLD.Send(taskInfo, 0, 2, MPI.INT, COORD_RANK, MSG_TAG_RESULT);
            return;
        }

        double[][] subMatM = new double[myRowCount][DIM_N];
        double[][] fullMatN = new double[DIM_N][DIM_P];
        double[][] subMatP = new double[myRowCount][DIM_P];

        for (int i = 0; i < subMatP.length; i++) {
            for (int j = 0; j < subMatP[0].length; j++) {
                subMatP[i][j] = 0.0;
            }
        }

        Request recvMHandle = MPI.COMM_WORLD.Irecv(subMatM, 0, myRowCount, MPI.OBJECT, COORD_RANK, MSG_TAG_TASK);
        Request recvNHandle = MPI.COMM_WORLD.Irecv(fullMatN, 0, DIM_N, MPI.OBJECT, COORD_RANK, MSG_TAG_TASK);

        Request[] dataReceiveHandles = {recvMHandle, recvNHandle};
        Request.Waitall(dataReceiveHandles);

        performLocalProduct(subMatM, fullMatN, subMatP);

        MPI.COMM_WORLD.Send(taskInfo, 0, 2, MPI.INT, COORD_RANK, MSG_TAG_RESULT);
        MPI.COMM_WORLD.Send(subMatP, 0, myRowCount, MPI.OBJECT, COORD_RANK, MSG_TAG_RESULT);
    }


    private static void performLocalProduct(double[][] matrixPartA, double[][] matrixB, double[][] resultPartC) {
        int rowsA = matrixPartA.length;
        if (rowsA == 0) return;
        int colsA = matrixPartA[0].length;
        int colsB = matrixB[0].length;

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                double elementSum = 0.0;
                for (int k = 0; k < colsA; k++) {
                    elementSum += matrixPartA[i][k] * matrixB[k][j];
                }
                resultPartC[i][j] = elementSum;
            }
        }
    }

    private static void verifyComputation(double[][] matrixM, double[][] matrixN, double[][] computedMatP) {
        if (matrixM == null || matrixN == null) {
            System.err.println("Verification cannot proceed: Input matrices not available.");
            return;
        }
        System.out.println("Performing sequential multiplication for verification...");
        double[][] referenceMatP = new double[DIM_M][DIM_P];
        performLocalProduct(matrixM, matrixN, referenceMatP);
        System.out.println("Sequential multiplication finished.");

        double tolerance = 1e-9;
        boolean match = true;
        for (int i = 0; i < DIM_M; i++) {
            for (int j = 0; j < DIM_P; j++) {
                if (Math.abs(computedMatP[i][j] - referenceMatP[i][j]) > tolerance) {
                    System.err.printf("Verification FAILED at index [%d][%d]. Parallel=%.6f, Reference=%.6f\n",
                            i, j, computedMatP[i][j], referenceMatP[i][j]);
                    match = false;
                    break;
                }
            }
            if (!match) break;
        }

        if (match) {
            System.out.println("Verification PASSED! Results match sequential computation within tolerance.");
        } else {
            System.out.println("Verification FAILED! Results do not match.");
        }
    }
}
