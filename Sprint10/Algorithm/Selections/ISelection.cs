namespace GeneticAlgorithm.Algorithm.Selections;

public interface ISelection
{
    public int Size { get; set; }
    public IList<Chromosome> Select(IList<Chromosome> population, int numberOfChromosomesToSelect);
}
