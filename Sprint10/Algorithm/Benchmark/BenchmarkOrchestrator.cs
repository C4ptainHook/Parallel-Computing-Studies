using System.Text;
using GeneticAlgorithm.Algorithm.Crossovers;
using GeneticAlgorithm.Algorithm.ParallelProcessing;
using GeneticAlgorithm.Algorithm.Selections;
using GeneticAlgorithm.Algorithm.Sequential;
using GeneticAlgorithm.Problem;

namespace GeneticAlgorithm.Algorithm.Benchmark;

public class BenchmarkOrchestrator
{
    private readonly string _storeDirectory = "./Artifacts";

    public void RunSequentialTests()
    {
        var problemItemCounts = new[] { 500, 1000, 1500, 2000 };
        var populationSizes = new[] { 200, 500, 1000 };
        var problemGenerator = new KnapsackProblemGenerator(seed: 38);

        var benchmarkRunner = new BenchmarkRunner(defaultWarmupRuns: 5, defaultMeasurementRuns: 1);
        var allResults = new List<BenchmarkResult>();

        foreach (var itemCount in problemItemCounts)
        {
            if (itemCount < problemItemCounts[0])
            {
                benchmarkRunner.SetWarmupRuns(0);
            }
            var problem = problemGenerator.Generate(itemCount: itemCount, capacityRatio: 0.5);
            Console.WriteLine(
                $"Problem generated: {problem.Items.Count} items, Capacity {problem.Capacity}, UpperBound {problem.CalculateUpperBound():F2}"
            );

            foreach (var currentPopulationSize in populationSizes)
            {
                Console.WriteLine(
                    $"Running sequential test with ItemCount: {itemCount}, PopulationSize: {currentPopulationSize}"
                );
                var seqConfig = new GeneticAlgorithmConfiguration(
                    populationSize: currentPopulationSize,
                    maxGenerations: 500
                )
                    .WithEliteCount((int)(currentPopulationSize * 0.3))
                    .WithSelection(new TournamentSelection((int)(currentPopulationSize * 0.02)))
                    .WithCrossover(new UniformCrossover());
                var sequentialGA = new SequentialAlgorithm(problem, seqConfig);
                var seqResults = benchmarkRunner.Execute<
                    SequentialAlgorithm,
                    GeneticAlgorithmConfiguration
                >(sequentialGA);
                allResults.AddRange(seqResults);
            }
        }

        SaveResultsToCsv(allResults, "Sequential_GridSearch_ProblemVsPopulation");
    }

    public void RunParallelTests()
    {
        var problemItemCounts = new[] { 500, 1000, 1500, 2000 };
        var populationSizes = new[] { 200, 500, 1000 };
        var threadCounts = new[] { 10, 12, 14 };
        var problemGenerator = new KnapsackProblemGenerator(seed: 110);

        var benchmarkRunner = new BenchmarkRunner(defaultWarmupRuns: 0, defaultMeasurementRuns: 1);
        var allResults = new List<BenchmarkResult>();

        foreach (var itemCount in problemItemCounts)
        {
            var problem = problemGenerator.Generate(itemCount: itemCount, capacityRatio: 0.5);
            Console.WriteLine(
                $"Problem generated: {problem.Items.Count} items, Capacity {problem.Capacity}, UpperBound {problem.CalculateUpperBound():F2}"
            );

            foreach (var currentPopulationSize in populationSizes)
            {
                foreach (var currentThreadCount in threadCounts)
                {
                    Console.WriteLine(
                        $"Running parallel test with ItemCount: {itemCount}, PopulationSize: {currentPopulationSize}, Threads: {currentThreadCount}"
                    );

                    var tournamentSize = Math.Max(2, (int)(currentPopulationSize * 0.02));

                    var parConfig = (ParallelGeneticAlgorithmConfiguration)
                        new ParallelGeneticAlgorithmConfiguration(
                            populationSize: currentPopulationSize,
                            maxGenerations: 500,
                            numberOfThreads: currentThreadCount
                        )
                            .WithAsyncGenerationsCount(50)
                            .WithWorkingMemoryCapacity(
                                Math.Max(
                                    10,
                                    itemCount / (currentPopulationSize / currentThreadCount)
                                )
                            )
                            .WithEliteCount(
                                Math.Max(2, (int)(currentPopulationSize * 0.2 / currentThreadCount))
                            )
                            .WithSelection(new TournamentSelection(tournamentSize))
                            .WithCrossover(new UniformCrossover());

                    var parallelGA = new PartiallyAsyncParallelAlgorithm(problem, parConfig);
                    var parResults = benchmarkRunner.Execute<
                        PartiallyAsyncParallelAlgorithm,
                        ParallelGeneticAlgorithmConfiguration
                    >(parallelGA);
                    allResults.AddRange(parResults);
                }
            }
        }
        SaveResultsToCsv(
            allResults,
            $"Parallel_GridSearch_ProblemVsPopulationVsThreads_{DateTime.Now:yyyyMMdd_HHmmss}"
        );
    }

    public void SaveResultsToCsv(IEnumerable<BenchmarkResult> results, string algorithmName)
    {
        var filename = $"{algorithmName}_{DateTime.Now:yyyyMMdd_HHmmss}.csv";
        var filePath = Path.Combine(_storeDirectory, filename);

        var directory = Path.GetDirectoryName(filePath);
        if (!string.IsNullOrEmpty(directory) && !Directory.Exists(directory))
        {
            Directory.CreateDirectory(directory);
        }
        using (var writer = new StreamWriter(File.Create(filePath), Encoding.UTF8))
        {
            writer.WriteLine(BenchmarkResult.GetCsvHeader());
            foreach (var result in results)
            {
                writer.WriteLine(result.ToCsvRow());
            }
        }
        Console.WriteLine($"Results saved to {filePath}");
    }
}
