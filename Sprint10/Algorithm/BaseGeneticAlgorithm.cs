using GeneticAlgorithm.Algorithm.Sequential;
using GeneticAlgorithm.Problem;

namespace GeneticAlgorithm.Algorithm.Sequential;

public abstract class BaseGeneticAlgorithm<TConfiguration>
    where TConfiguration : GeneticAlgorithmConfiguration
{
    protected readonly KnapsackProblem Problem;
    protected readonly TConfiguration Configuration;
    protected List<int> SortedItemIndicesByDensity = [];
    private readonly Random _random = new();
    protected int CurrentGeneration = 0;
    protected double BestFitness = double.MinValue;

    public BaseGeneticAlgorithm(KnapsackProblem problem, TConfiguration configuration)
    {
        Problem = problem;
        Configuration = configuration;
        InitializeDensitySortedIndices();
    }

    private void InitializeDensitySortedIndices()
    {
        int numberOfItems = Problem.Items.Count;
        var itemsWithDensity = new List<(int index, double density)>(numberOfItems);
        for (int i = 0; i < numberOfItems; i++)
        {
            double density = 0;
            if (Problem.Items[i].Weight > 0)
            {
                density = Problem.Items[i].Value / Problem.Items[i].Weight;
            }
            else if (Problem.Items[i].Value > 0)
            {
                density = double.MaxValue;
            }
            itemsWithDensity.Add((i, density));
        }

        itemsWithDensity.Sort((x, y) => y.density.CompareTo(x.density));
        SortedItemIndicesByDensity = [.. itemsWithDensity.Select(item => item.index)];
    }

    protected virtual IList<Chromosome> CreateInitialPopulation()
    {
        int populationSize = Configuration.PopulationSize;
        var population = new List<Chromosome>(populationSize);

        for (int i = 0; i < populationSize; i++)
        {
            population.Add(GenerateHeuristicChromosome());
        }
        return population;
    }

    private Chromosome GenerateHeuristicChromosome()
    {
        int numberOfGenes = Problem.Items.Count;
        var chromosome = new Chromosome(numberOfGenes);
        int currentWeight = 0;
        var itemTaken = new bool[numberOfGenes];
        int rclConstructionSize = Math.Max(1, Configuration.RclSize);

        for (int iter = 0; iter < numberOfGenes; iter++)
        {
            var restrictedCandidateList = new List<int>();
            foreach (int itemIndex in SortedItemIndicesByDensity)
            {
                if (!itemTaken[itemIndex])
                {
                    if (currentWeight + Problem.Items[itemIndex].Weight <= Problem.Capacity)
                    {
                        restrictedCandidateList.Add(itemIndex);
                        if (restrictedCandidateList.Count >= rclConstructionSize)
                        {
                            break;
                        }
                    }
                }
            }

            if (restrictedCandidateList.Count == 0)
            {
                break;
            }

            int chosenItemIndex = restrictedCandidateList[
                _random.Next(restrictedCandidateList.Count)
            ];

            chromosome[chosenItemIndex] = true;
            itemTaken[chosenItemIndex] = true;
            currentWeight += Problem.Items[chosenItemIndex].Weight;
        }
        return chromosome;
    }

    public abstract Chromosome Run();

    public TConfiguration GetConfiguration()
    {
        return Configuration;
    }

    public KnapsackProblem GetProblem()
    {
        return Problem;
    }

    public void Reset()
    {
        CurrentGeneration = 0;
        BestFitness = double.MinValue;
        SortedItemIndicesByDensity.Clear();
        InitializeDensitySortedIndices();
    }
}
