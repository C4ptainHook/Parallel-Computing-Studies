using GeneticAlgorithm.Algorithm;
using GeneticAlgorithm.Algorithm.Benchmark;
using GeneticAlgorithm.Problem;

namespace GeneticAlgorithm;

public class Program
{
    public static void Main(string[] args)
    {
        var benchmarkOrchestrator = new BenchmarkOrchestrator();
        // benchmarkOrchestrator.RunSequentialTests();
        benchmarkOrchestrator.RunParallelTests();
    }
}
