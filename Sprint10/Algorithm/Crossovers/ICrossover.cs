namespace GeneticAlgorithm.Algorithm.Crossovers;

public interface ICrossover
{
    Chromosome Cross(Chromosome leftParent, Chromosome rightParent);
}
