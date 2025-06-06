namespace GeneticAlgorithm.Algorithm.Reintegrations;

public interface IReintegration
{
    IList<Chromosome> Reintegrate(
        IList<Chromosome> populationToUpdate,
        IList<Chromosome> newOffspring,
        int eliteCount
    );
}
