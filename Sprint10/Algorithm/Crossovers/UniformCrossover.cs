namespace GeneticAlgorithm.Algorithm.Crossovers;

public class UniformCrossover : ICrossover
{
    private readonly Random _random = new();

    public Chromosome Cross(Chromosome leftParent, Chromosome rightParent)
    {
        var child = leftParent.Clone();

        for (int i = 0; i < leftParent.Length; i++)
        {
            if (_random.NextDouble() < 0.5)
            {
                child[i] = rightParent[i];
            }
        }
        return child;
    }
}
