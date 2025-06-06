namespace GeneticAlgorithm.Algorithm.Reintegrations;

public class GenerationalReintegration : IReintegration
{
    public IList<Chromosome> Reintegrate(
        IList<Chromosome> populationToUpdate,
        IList<Chromosome> newOffspring,
        int eliteCount
    )
    {
        var nextGenerationChromosomes = new List<Chromosome>();
        if (eliteCount > 0 && populationToUpdate.Any())
        {
            nextGenerationChromosomes.AddRange(
                populationToUpdate
                    .OrderByDescending(c => c.Fitness)
                    .Take(eliteCount)
                    .Select(c => c.Clone())
            );
        }

        int spotsForOffspring = populationToUpdate.Count - nextGenerationChromosomes.Count;
        if (spotsForOffspring > 0 && newOffspring.Any())
        {
            nextGenerationChromosomes.AddRange(
                newOffspring
                    .OrderByDescending(o => o.Fitness)
                    .Take(spotsForOffspring)
                    .Select(o => o.Clone())
            );
        }
        return nextGenerationChromosomes;
    }
}
