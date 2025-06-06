using GeneticAlgorithm.Algorithm.Crossovers;
using GeneticAlgorithm.Algorithm.Mutations;
using GeneticAlgorithm.Algorithm.Reintegrations;
using GeneticAlgorithm.Algorithm.Selections;

namespace GeneticAlgorithm.Algorithm.Sequential;

public class GeneticAlgorithmConfiguration(int populationSize, int maxGenerations)
{
    public int PopulationSize { get; protected set; } = populationSize;
    public int EliteCount { get; protected set; } = 2;
    public ISelection Selection { get; protected set; } = new TournamentSelection();
    public ICrossover Crossover { get; protected set; } = new TwoPointCrossover();
    public IMutation Mutation { get; protected set; } = new UniformFlipMutation();
    public IReintegration Reinsertion { get; protected set; } = new GenerationalReintegration();
    public int RclSize { get; protected set; } = 2;
    public int MaxGenerations { get; protected set; } = maxGenerations;

    public GeneticAlgorithmConfiguration WithEliteCount(int value)
    {
        if (value < 0)
            throw new ArgumentException("Elite count must be non-negative.", nameof(value));
        var copy = Clone();
        copy.EliteCount = value;
        return copy;
    }

    public GeneticAlgorithmConfiguration WithSelection(ISelection selection)
    {
        var copy = Clone();
        copy.Selection = selection ?? throw new ArgumentNullException(nameof(selection));
        return copy;
    }

    public GeneticAlgorithmConfiguration WithCrossover(ICrossover crossover)
    {
        var copy = Clone();
        copy.Crossover = crossover ?? throw new ArgumentNullException(nameof(crossover));
        return copy;
    }

    public GeneticAlgorithmConfiguration WithMutation(IMutation mutation)
    {
        var copy = Clone();
        copy.Mutation = mutation ?? throw new ArgumentNullException(nameof(mutation));
        return copy;
    }

    public GeneticAlgorithmConfiguration WithReintegration(IReintegration reinsertion)
    {
        var copy = Clone();
        copy.Reinsertion = reinsertion ?? throw new ArgumentNullException(nameof(reinsertion));
        return copy;
    }

    public GeneticAlgorithmConfiguration WithMaxGenerations(int value)
    {
        if (value <= 0)
            throw new ArgumentException(
                "Max generations must be greater than zero.",
                nameof(value)
            );
        var copy = Clone();
        copy.MaxGenerations = value;
        return copy;
    }

    public GeneticAlgorithmConfiguration WithRCLSize(int value)
    {
        if (value <= 0)
            throw new ArgumentException("RCL size must be greater than zero.", nameof(value));
        var copy = Clone();
        copy.RclSize = value;
        return copy;
    }

    protected virtual GeneticAlgorithmConfiguration Clone()
    {
        return new GeneticAlgorithmConfiguration(PopulationSize, MaxGenerations)
        {
            EliteCount = EliteCount,
            Selection = Selection,
            Crossover = Crossover,
            Mutation = Mutation,
            RclSize = RclSize,
        };
    }
}
