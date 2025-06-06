namespace GeneticAlgorithm.Algorithm.ParallelProcessing;

public static class WorkingMemorySlotsExtensions
{
    public static IList<Chromosome> GetLatestSubPopulation(
        this ChromosomeWorkingMemorySlot[] memorySlots,
        int startIndex,
        int endIndex
    )
    {
        var latestSubPopulation = new Chromosome[endIndex - startIndex];
        for (int i = startIndex; i < endIndex; i++)
        {
            latestSubPopulation[i - startIndex] = memorySlots[i].GetLatestVersion();
        }
        return latestSubPopulation;
    }

    public static void Reintegrate(
        this ChromosomeWorkingMemorySlot[] memorySlots,
        IList<Chromosome> newOffspring,
        int startIndex,
        int endIndex
    )
    {
        for (int i = startIndex; i < endIndex; i++)
        {
            memorySlots[i].AddVersion(newOffspring[i - startIndex]);
        }
    }

    public static Chromosome GetLatestBestSolution(this ChromosomeWorkingMemorySlot[] memorySlots)
    {
        return memorySlots
            .GetLatestSubPopulation(0, memorySlots.Length)
            .OrderByDescending(chromosome => chromosome.Fitness)
            .First();
    }
}
