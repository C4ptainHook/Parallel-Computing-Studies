using GeneticAlgorithm.Algorithm.Sequential;

namespace GeneticAlgorithm.Algorithm.ParallelProcessing;

public class ParallelGeneticAlgorithmConfiguration : GeneticAlgorithmConfiguration
{
    public int NumberOfThreads { get; private set; }
    public int WorkingMemoryCapacity { get; private set; } = 10;
    public int AsynchronousGenerationCount { get; private set; } = 10;

    public ParallelGeneticAlgorithmConfiguration(
        int populationSize,
        int maxGenerations,
        int numberOfThreads
    )
        : base(populationSize, maxGenerations)
    {
        if (numberOfThreads <= 0)
            throw new ArgumentOutOfRangeException(
                nameof(numberOfThreads),
                "Number of threads must be greater than zero."
            );

        NumberOfThreads = numberOfThreads;
    }

    public ParallelGeneticAlgorithmConfiguration WithWorkingMemoryCapacity(
        int workingMemoryCapacity
    )
    {
        if (workingMemoryCapacity <= 0)
            throw new ArgumentOutOfRangeException(
                nameof(workingMemoryCapacity),
                "Working memory capacity must be greater than zero."
            );

        var copy = Clone();
        copy.WorkingMemoryCapacity = workingMemoryCapacity;
        return copy;
    }

    public ParallelGeneticAlgorithmConfiguration WithAsyncGenerationsCount(
        int asynchronousGenerationCount
    )
    {
        if (asynchronousGenerationCount <= 0)
            throw new ArgumentOutOfRangeException(
                nameof(asynchronousGenerationCount),
                "Asynchronous generation count must be greater than zero."
            );

        var copy = Clone();
        copy.AsynchronousGenerationCount = asynchronousGenerationCount;
        return copy;
    }

    protected override ParallelGeneticAlgorithmConfiguration Clone()
    {
        return new ParallelGeneticAlgorithmConfiguration(
            PopulationSize,
            MaxGenerations,
            NumberOfThreads
        )
        {
            EliteCount = EliteCount,
            Selection = Selection,
            Crossover = Crossover,
            Mutation = Mutation,
            RclSize = RclSize,
            WorkingMemoryCapacity = WorkingMemoryCapacity,
            AsynchronousGenerationCount = AsynchronousGenerationCount,
        };
    }
}
