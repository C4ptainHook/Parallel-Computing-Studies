using System.Diagnostics;
using GeneticAlgorithm.Algorithm.ParallelProcessing;
using GeneticAlgorithm.Algorithm.Selections;
using GeneticAlgorithm.Algorithm.Sequential;

namespace GeneticAlgorithm.Algorithm.Benchmark;

public class BenchmarkRunner(int defaultWarmupRuns = 1, int defaultMeasurementRuns = 5)
{
    private int _warmupRuns = Math.Max(0, defaultWarmupRuns);
    private int _measurementRuns = Math.Max(1, defaultMeasurementRuns);

    public List<BenchmarkResult> Execute<TAlgorithmType, TConfiguration>(TAlgorithmType gaInstance)
        where TAlgorithmType : BaseGeneticAlgorithm<TConfiguration>
        where TConfiguration : GeneticAlgorithmConfiguration
    {
        if (gaInstance == null)
        {
            throw new ArgumentNullException(nameof(gaInstance), "GA instance cannot be null.");
        }

        var problem = gaInstance.GetProblem();
        var configuration = gaInstance.GetConfiguration();
        var results = new List<BenchmarkResult>();
        var gaType = gaInstance.GetType().Name;

        Console.WriteLine($"--- Starting Benchmark for: {gaType} GA ---");
        Console.WriteLine($"Problem: {problem.Items.Count} items, Capacity {problem.Capacity}");
        Console.WriteLine(
            $"Config: PopSize {configuration.PopulationSize}, MaxGens {configuration.MaxGenerations}"
        );
        if (configuration is ParallelGeneticAlgorithmConfiguration pConfigWarmup)
        {
            Console.WriteLine(
                $"        Threads {pConfigWarmup.NumberOfThreads}, AsyncGens {pConfigWarmup.AsynchronousGenerationCount}"
            );
        }

        if (_warmupRuns > 0)
        {
            Console.WriteLine($"Performing {_warmupRuns} warm-up runs...");
            for (int i = 0; i < _warmupRuns; i++)
            {
                gaInstance.Run();
                gaInstance.Reset();
            }
            Console.WriteLine("Warm-up complete.");
        }

        Console.WriteLine($"Performing {_measurementRuns} measurement runs...");
        double problemUpperBound = problem.CalculateUpperBound();

        for (int i = 0; i < _measurementRuns; i++)
        {
            Console.Write($"  Run {i + 1}/{_measurementRuns}... ");
            var stopwatch = Stopwatch.StartNew();
            Chromosome bestChromosome = gaInstance.Run();
            if (!problem.CheckChromosomeValidity(bestChromosome))
            {
                throw new InvalidOperationException(
                    "The best solution found is not valid according to the problem constraints."
                );
            }
            Console.WriteLine($"Solution is valid ✓ ✓ ✓ . Fitness: {bestChromosome.Fitness:F2}");
            stopwatch.Stop();

            var result = new BenchmarkResult
            {
                ProblemItemCount = problem.Items.Count,
                ProblemCapacity = problem.Capacity,
                ProblemTheoreticalUpperBound = problemUpperBound,
                GAType = gaType,
                ConfigPopulationSize = configuration.PopulationSize,
                ConfigMaxGenerations = configuration.MaxGenerations,
                ConfigEliteCount = configuration.EliteCount,
                ConfigSelectionType = configuration.Selection?.GetType().Name ?? "N/A",
                ConfigCrossoverType = configuration.Crossover?.GetType().Name ?? "N/A",
                ConfigMutationType = configuration.Mutation?.GetType().Name ?? "N/A",
                ConfigReintegrationType = configuration.Reinsertion?.GetType().Name ?? "N/A",
                ConfigRclSize = configuration.RclSize,
                RunNumber = i + 1,
                ExecutionTimeMilliseconds = stopwatch.ElapsedMilliseconds,
                BestFitnessAchieved = bestChromosome?.Fitness ?? double.MinValue,
            };
            result.OptimalityRatio = result.BestFitnessAchieved / problemUpperBound;

            if (configuration.Selection is TournamentSelection tournamentSelection)
            {
                result.ConfigTournamentSize = configuration.Selection.Size;
            }

            if (configuration is ParallelGeneticAlgorithmConfiguration pConfig)
            {
                result.ConfigParNumberOfThreads = pConfig.NumberOfThreads;
                result.ConfigParWorkingMemoryCapacity = pConfig.WorkingMemoryCapacity;
                result.ConfigParAsyncGenCount = pConfig.AsynchronousGenerationCount;
            }

            results.Add(result);
            Console.WriteLine(
                $"Done. Time: {stopwatch.ElapsedMilliseconds}ms, Fitness: {result.BestFitnessAchieved:F2}, Ratio: {result.OptimalityRatio:P4}"
            );
            gaInstance.Reset();
        }
        Console.WriteLine($"--- Benchmark Complete for: {gaType} GA ---");
        return results;
    }

    public void SetWarmupRuns(int warmupRuns)
    {
        _warmupRuns = Math.Max(0, warmupRuns);
    }
}
