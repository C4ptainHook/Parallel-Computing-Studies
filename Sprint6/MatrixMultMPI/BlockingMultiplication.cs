using System.Diagnostics;
using MPI;

class BlockingMultiplication
{
    const int NRA = 2500;
    const int NCA = 2500;
    const int NCB = 2500;

    const int MASTER = 0;
    const int FROM_MASTER = 1;
    const int FROM_WORKER = 2;

    static void Main(string[] args)
    {
        using (new MPI.Environment(ref args))
        {
            Intracommunicator comm = Communicator.world;
            int numtasks = comm.Size;
            int taskid = comm.Rank;
            int numworkers = numtasks - 1;

            if (numtasks < 2)
            {
                Console.WriteLine("Need at least two MPI tasks. Quitting...");
                return;
            }

            if (taskid == MASTER)
            {
                ExecuteMasterTask(comm, numtasks, numworkers);
            }
            else
            {
                ExecuteWorkerTask(comm, taskid);
            }
        }
    }

    private static void ExecuteMasterTask(Intracommunicator comm, int numtasks, int numworkers)
    {
        Console.WriteLine($"mpi_mm started with {numtasks} tasks.");

        double[,] a = new double[NRA, NCA];
        double[,] b = new double[NCA, NCB];
        double[,] c = new double[NRA, NCB];

        InitializeMatrices(a, b);

        Stopwatch parallelStopwatch = Stopwatch.StartNew();

        DistributeWorkToWorkers(comm, numworkers, a, b);
        CollectResultsFromWorkers(comm, numworkers, c);

        parallelStopwatch.Stop();
        double parallelTime = parallelStopwatch.ElapsedMilliseconds / 1000.0;
        Console.WriteLine($"Parallel MPI execution time: {parallelTime:F6} seconds");

        ValidateResults(a, b, c, parallelTime);
    }

    private static void InitializeMatrices(double[,] a, double[,] b)
    {
        for (int i = 0; i < NRA; i++)
        for (int j = 0; j < NCA; j++)
            a[i, j] = 10.0;

        for (int i = 0; i < NCA; i++)
        for (int j = 0; j < NCB; j++)
            b[i, j] = 10.0;
    }

    private static void DistributeWorkToWorkers(
        Intracommunicator comm,
        int numworkers,
        double[,] a,
        double[,] b
    )
    {
        int averow = NRA / numworkers;
        int extra = NRA % numworkers;
        int offset = 0;

        double[] flatB = Flatten(b);

        for (int dest = 1; dest <= numworkers; dest++)
        {
            int rows = (dest <= extra) ? averow + 1 : averow;

            Console.WriteLine($"Sending {rows} rows to task {dest} offset={offset}");

            comm.Send(offset, dest, FROM_MASTER);
            comm.Send(rows, dest, FROM_MASTER);

            double[,] aChunk = new double[rows, NCA];
            for (int i = 0; i < rows; i++)
            for (int j = 0; j < NCA; j++)
                aChunk[i, j] = a[offset + i, j];
            comm.Send(Flatten(aChunk), dest, FROM_MASTER);

            comm.Send(flatB, dest, FROM_MASTER);

            offset += rows;
        }
    }

    private static void CollectResultsFromWorkers(
        Intracommunicator comm,
        int numworkers,
        double[,] c
    )
    {
        for (int source = 1; source <= numworkers; source++)
        {
            int workerOffset = comm.Receive<int>(source, FROM_WORKER);
            int rows = comm.Receive<int>(source, FROM_WORKER);
            double[] flatC = new double[rows * NCB];
            comm.Receive(source, FROM_WORKER, ref flatC);

            double[,] cChunk = Unflatten(flatC, rows, NCB);
            for (int i = 0; i < rows; i++)
            for (int j = 0; j < NCB; j++)
                c[workerOffset + i, j] = cChunk[i, j];

            Console.WriteLine($"Received results from task {source}");
        }
    }

    private static void MultiplyMatricesSequential(double[,] a, double[,] b, double[,] c)
    {
        int rowsA = a.GetLength(0);
        int colsB = b.GetLength(1);
        int colsA = a.GetLength(1);

        for (int i = 0; i < rowsA; i++)
        {
            for (int j = 0; j < colsB; j++)
            {
                for (int k = 0; k < colsA; k++)
                {
                    c[i, j] += a[i, k] * b[k, j];
                }
            }
        }
    }

    private static void ValidateResults(double[,] a, double[,] b, double[,] c, double parallelTime)
    {
        double[,] expected = new double[a.GetLength(0), b.GetLength(1)];

        Stopwatch sequentialStopwatch = Stopwatch.StartNew();
        MultiplyMatricesSequential(a, b, expected);
        sequentialStopwatch.Stop();
        double sequentialTime = sequentialStopwatch.ElapsedMilliseconds / 1000.0;

        Console.WriteLine($"Sequential execution time: {sequentialTime:F6} seconds");

        // Calculate speedup
        double speedup = sequentialTime / parallelTime;
        Console.WriteLine($"Speedup: {speedup:F2}x");

        for (int i = 0; i < a.GetLength(0); i++)
        {
            for (int j = 0; j < b.GetLength(1); j++)
            {
                if (Math.Abs(c[i, j] - expected[i, j]) > 1e-9)
                {
                    throw new Exception(
                        $"Matrix multiplication result is incorrect at [{i},{j}]! Expected: {expected[i, j]}, Got: {c[i, j]}"
                    );
                }
            }
        }
        Console.WriteLine("Validation passed! MPI result matches sequential calculation.");
    }

    private static void ExecuteWorkerTask(Intracommunicator comm, int taskid)
    {
        try
        {
            int offset = ReceiveOffsetAndRows(comm, taskid, out int rows);
            double[,] aChunk = ReceiveMatrixA(comm, taskid, rows);
            double[,] bMatrix = ReceiveMatrixB(comm, taskid);
            double[,] cChunk = MultiplyMatrices(rows, aChunk, bMatrix);
            SendResultsToMaster(comm, taskid, offset, rows, cChunk);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[Task {taskid}] ERROR: {ex.Message}");
            throw;
        }
    }

    private static int ReceiveOffsetAndRows(Intracommunicator comm, int taskid, out int rows)
    {
        Console.WriteLine($"[Task {taskid}] Receiving offset...");
        int offset = comm.Receive<int>(MASTER, FROM_MASTER);

        Console.WriteLine($"[Task {taskid}] Receiving row count...");
        rows = comm.Receive<int>(MASTER, FROM_MASTER);

        return offset;
    }

    private static double[,] ReceiveMatrixA(Intracommunicator comm, int taskid, int rows)
    {
        Console.WriteLine($"[Task {taskid}] Receiving flat A chunk...");
        double[] flatAChunk = new double[rows * NCA];
        comm.Receive(MASTER, FROM_MASTER, ref flatAChunk);
        return Unflatten(flatAChunk, rows, NCA);
    }

    private static double[,] ReceiveMatrixB(Intracommunicator comm, int taskid)
    {
        Console.WriteLine($"[Task {taskid}] Receiving flat B matrix...");
        double[] flatB = new double[NCA * NCB];
        comm.Receive(MASTER, FROM_MASTER, ref flatB);
        return Unflatten(flatB, NCA, NCB);
    }

    private static double[,] MultiplyMatrices(int rows, double[,] aChunk, double[,] bMatrix)
    {
        double[,] cChunk = new double[rows, NCB];

        for (int k = 0; k < NCB; k++)
        for (int i = 0; i < rows; i++)
        {
            cChunk[i, k] = 0.0;
            for (int j = 0; j < NCA; j++)
                cChunk[i, k] += aChunk[i, j] * bMatrix[j, k];
        }

        return cChunk;
    }

    private static void SendResultsToMaster(
        Intracommunicator comm,
        int taskid,
        int offset,
        int rows,
        double[,] cChunk
    )
    {
        Console.WriteLine($"[Task {taskid}] Sending results back...");
        double[] flatC = Flatten(cChunk);
        comm.Send(offset, MASTER, FROM_WORKER);
        comm.Send(rows, MASTER, FROM_WORKER);
        comm.Send(flatC, MASTER, FROM_WORKER);

        Console.WriteLine($"[Task {taskid}] Done.");
    }

    static double[] Flatten(double[,] matrix)
    {
        int rows = matrix.GetLength(0);
        int cols = matrix.GetLength(1);
        double[] flat = new double[rows * cols];
        Buffer.BlockCopy(matrix, 0, flat, 0, flat.Length * sizeof(double));
        return flat;
    }

    static double[,] Unflatten(double[] flat, int rows, int cols)
    {
        double[,] matrix = new double[rows, cols];
        Buffer.BlockCopy(flat, 0, matrix, 0, flat.Length * sizeof(double));
        return matrix;
    }
}
