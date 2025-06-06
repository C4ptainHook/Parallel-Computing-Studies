namespace GeneticAlgorithm.Algorithm.Mutations;

public interface IMutation
{
    Chromosome Mutate(Chromosome chromosome, double mutationProbability);
}
