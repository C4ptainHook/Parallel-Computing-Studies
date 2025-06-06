using System.Diagnostics;
using MPI;
using System.Collections.Generic;

class NonBlockingMultiplication
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
        Console.WriteLine($"mpi_mm (non-blocking) started with {numtasks} tasks.");

        double[,] a = new double[NRA, NCA];
        double[,] b = new double[NCA, NCB];
        double[,] c = new double[NRA, NCB];

        InitializeMatrices(a, b);

        Stopwatch parallelStopwatch = Stopwatch.StartNew();

        DistributeWorkToWorkersNonBlocking(comm, numworkers, a, b);
        CollectResultsFromWorkersNonBlocking(comm, numworkers, c);

        parallelStopwatch.Stop();
        double parallelTime = parallelStopwatch.ElapsedMilliseconds / 1000.0;
        Console.WriteLine($"Parallel MPI (non-blocking) execution time: {parallelTime:F6} seconds");

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

    private static void DistributeWorkToWorkersNonBlocking(
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
        
        // Store all buffers to prevent garbage collection during non-blocking operations
        var bufferStore = new List<double[]>();
        bufferStore.Add(flatB);
        
        // Store all requests to wait for completion
        List<Request> requests = new List<Request>();

        for (int dest = 1; dest <= numworkers; dest++)
        {
            int rows = (dest <= extra) ? averow + 1 : averow;
            Console.WriteLine($"Sending {rows} rows to task {dest} offset={offset}");

            try
            {
                // Send metadata
                requests.Add(comm.ImmediateSend(offset, dest, FROM_MASTER));
                requests.Add(comm.ImmediateSend(rows, dest, FROM_MASTER));
                
                // Prepare and send matrix chunk
                double[,] aChunk = new double[rows, NCA];
                for (int i = 0; i < rows; i++)
                for (int j = 0; j < NCA; j++)
                    aChunk[i, j] = a[offset + i, j];
                
                double[] flatAChunk = Flatten(aChunk);
                bufferStore.Add(flatAChunk); // Keep reference to prevent garbage collection
                
                requests.Add(comm.ImmediateSend(flatAChunk, dest, FROM_MASTER));
                requests.Add(comm.ImmediateSend(flatB, dest, FROM_MASTER));
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error sending data to worker {dest}: {ex.Message}");
                throw;
            }

            offset += rows;
        }

        // Wait for all sends to complete
        Request.WaitAll(requests.ToArray());
        Console.WriteLine("All data sent to workers successfully");
    }

    private static void CollectResultsFromWorkersNonBlocking(
        Intracommunicator comm,
        int numworkers,
        double[,] c
    )
    {
        // First, receive all metadata (offset and rows)
        int[] offsets = new int[numworkers];
        int[] rows = new int[numworkers];
        Request[] metaRequests = new Request[numworkers * 2];
        
        for (int i = 0; i < numworkers; i++)
        {
            int source = i + 1;
            metaRequests[i * 2] = comm.ImmediateReceive<int>(out offsets[i], source, FROM_WORKER);
            metaRequests[i * 2 + 1] = comm.ImmediateReceive<int>(out rows[i], source, FROM_WORKER);
        }
        
        // Wait for all metadata
        Request.WaitAll(metaRequests);
        Console.WriteLine("Received metadata from all workers");
        
        // Now receive all results data using the metadata we got
        double[][] resultData = new double[numworkers][];
        Request[] dataRequests = new Request[numworkers];
        
        for (int i = 0; i < numworkers; i++)
        {
            int source = i + 1;
            resultData[i] = new double[rows[i] * NCB]; // Preallocate result buffer
            dataRequests[i] = comm.ImmediateReceive<double[]>(out resultData[i], source, FROM_WORKER);
        }
        
        // Wait for all data
        Request.WaitAll(dataRequests);
        Console.WriteLine("Received all result data from workers");
        
        // Process the received data
        for (int i = 0; i < numworkers; i++)
        {
            int source = i + 1;
            double[,] cChunk = Unflatten(resultData[i], rows[i], NCB);
            
            for (int r = 0; r < rows[i]; r++)
            for (int j = 0; j < NCB; j++)
                c[offsets[i] + r, j] = cChunk[r, j];

            Console.WriteLine($"Processed results from task {source}");
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
                c[i, j] = 0; // Initialize to zero
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
            // Receive metadata with non-blocking operations
            int offset, rows;
            Request offsetRequest = comm.ImmediateReceive<int>(out offset, MASTER, FROM_MASTER);
            Request rowsRequest = comm.ImmediateReceive<int>(out rows, MASTER, FROM_MASTER);
            
            // Wait for metadata
            Request.WaitAll(new Request[] { offsetRequest, rowsRequest });
            Console.WriteLine($"[Task {taskid}] Received offset={offset}, rows={rows}");
            
            // Now receive matrix data
            double[] flatAChunk, flatB;
            Request aRequest = comm.ImmediateReceive<double[]>(out flatAChunk, MASTER, FROM_MASTER);
            Request bRequest = comm.ImmediateReceive<double[]>(out flatB, MASTER, FROM_MASTER);
            
            // Wait for matrix data
            Request.WaitAll(new Request[] { aRequest, bRequest });
            Console.WriteLine($"[Task {taskid}] Received matrix data");

            double[,] aChunk = Unflatten(flatAChunk, rows, NCA);
            double[,] bMatrix = Unflatten(flatB, NCA, NCB);

            // Multiply matrices
            double[,] cChunk = new double[rows, NCB];
            for (int k = 0; k < NCB; k++)
            for (int i = 0; i < rows; i++)
            {
                cChunk[i, k] = 0.0;
                for (int j = 0; j < NCA; j++)
                    cChunk[i, k] += aChunk[i, j] * bMatrix[j, k];
            }

            // Send results back using non-blocking operations
            double[] flatC = Flatten(cChunk);
            
            List<Request> sendRequests = new List<Request>();
            sendRequests.Add(comm.ImmediateSend(offset, MASTER, FROM_WORKER));
            sendRequests.Add(comm.ImmediateSend(rows, MASTER, FROM_WORKER));
            sendRequests.Add(comm.ImmediateSend(flatC, MASTER, FROM_WORKER));
            
            // Wait for all sends to complete
            Request.WaitAll(sendRequests.ToArray());
            Console.WriteLine($"[Task {taskid}] Done.");
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[Task {taskid}] ERROR: {ex.Message}");
            throw;
        }
    }

    static double[] Flatten(double[,] matrix)
    {
        int rows = matrix.GetLength(0);
        int cols = matrix.GetLength(1);
        double[] flat = new double[rows * cols];
        int index = 0;
        for (int r = 0; r < rows; r++)
        for (int c = 0; c < cols; c++)
            flat[index++] = matrix[r, c];
        return flat;
    }

    static double[,] Unflatten(double[] flat, int rows, int cols)
    {
        double[,] matrix = new double[rows, cols];
        int index = 0;
        for (int r = 0; r < rows; r++)
        for (int c = 0; c < cols; c++)
            matrix[r, c] = flat[index++];
        return matrix;
    }
}
