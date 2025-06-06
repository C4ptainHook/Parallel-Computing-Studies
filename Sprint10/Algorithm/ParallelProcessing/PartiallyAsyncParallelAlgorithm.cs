using GeneticAlgorithm.Algorithm.Sequential;
using GeneticAlgorithm.Problem;

namespace GeneticAlgorithm.Algorithm.ParallelProcessing;

public class PartiallyAsyncParallelAlgorithm(
    KnapsackProblem problem,
    ParallelGeneticAlgorithmConfiguration configuration
) : BaseGeneticAlgorithm<ParallelGeneticAlgorithmConfiguration>(problem, configuration)
{
    private ChromosomeWorkingMemorySlot[] _memorySlots = new ChromosomeWorkingMemorySlot[
        configuration.PopulationSize
    ];

    public override Chromosome Run()
    {
        var population = CreateInitialPopulation();
        for (int i = 0; i < Configuration.PopulationSize; i++)
        {
            var chromosome = population[i];
            chromosome.Fitness = Problem.EvaluateFitness(chromosome);
            _memorySlots[i] = new ChromosomeWorkingMemorySlot(Configuration.WorkingMemoryCapacity);
            _memorySlots[i].AddVersion(chromosome);
        }

        while (CurrentGeneration < Configuration.MaxGenerations)
        {
            Parallel.For(
                0,
                Configuration.NumberOfThreads,
                workerId =>
                {
                    int baseChunkSize =
                        Configuration.PopulationSize / Configuration.NumberOfThreads;
                    int remainder = Configuration.PopulationSize % Configuration.NumberOfThreads;
                    int startIndex = workerId * baseChunkSize + Math.Min(workerId, remainder);
                    int endIndex = startIndex + baseChunkSize + (workerId < remainder ? 1 : 0);
                    RunWorker(Configuration.AsynchronousGenerationCount, startIndex, endIndex);
                }
            );
            CurrentGeneration += Configuration.AsynchronousGenerationCount;
        }

        var bestChromosome = _memorySlots.GetLatestBestSolution();
        BestFitness = bestChromosome.Fitness;
        return bestChromosome;
    }

    public void RunWorker(int asyncGenerationCount, int startIndex, int endIndex)
    {
        IList<Chromosome> latestSolutions;
        for (int g = 0; g < asyncGenerationCount; g++)
        {
            var offsprings = new List<Chromosome>();
            for (int i = startIndex; i < endIndex; i++)
            {
                latestSolutions = _memorySlots.GetLatestSubPopulation(
                    0,
                    Configuration.PopulationSize
                );
                var parents = Configuration.Selection.Select(latestSolutions, 2);
                var left_child = Configuration.Crossover.Cross(parents[0], parents[1]);
                var right_child = Configuration.Crossover.Cross(parents[1], parents[0]);
                var mutated_left_child = Configuration.Mutation.Mutate(
                    left_child,
                    1.0 / Problem.Items.Count
                );
                var mutated_right_child = Configuration.Mutation.Mutate(
                    right_child,
                    1.0 / Problem.Items.Count
                );
                mutated_left_child.Fitness = Problem.EvaluateFitness(mutated_left_child);
                mutated_right_child.Fitness = Problem.EvaluateFitness(mutated_right_child);
                offsprings.Add(mutated_left_child);
                offsprings.Add(mutated_right_child);
            }
            latestSolutions = _memorySlots.GetLatestSubPopulation(startIndex, endIndex);

            var population = Configuration.Reinsertion.Reintegrate(
                latestSolutions,
                offsprings,
                Configuration.EliteCount
            );
            _memorySlots.Reintegrate(population, startIndex, endIndex);
        }
    }
}
