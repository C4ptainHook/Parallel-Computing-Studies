namespace GeneticAlgorithm.Algorithm.Mutations;

public class UniformFlipMutation() : IMutation
{
    private readonly Random _random = new();

    public Chromosome Mutate(Chromosome chromosome, double mutationProbability)
    {
        var chromosomeCLone = chromosome.Clone();
        for (int i = 0; i < chromosome.Length; i++)
        {
            if (_random.NextDouble() <= mutationProbability)
            {
                chromosomeCLone.FlipGene(i);
            }
        }
        return chromosomeCLone;
    }
}
