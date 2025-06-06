using GeneticAlgorithm.Algorithm;

namespace GeneticAlgorithm.Problem;

public class KnapsackProblem
{
    public List<Item> Items { get; }
    public int Capacity { get; }

    public KnapsackProblem(List<Item> items, int capacity)
    {
        Items = items ?? throw new ArgumentNullException(nameof(items));
        if (capacity <= 0)
            throw new ArgumentException("Capacity must be greater than zero", nameof(capacity));
        if (items.Count == 0)
            throw new ArgumentException("Items list cannot be empty", nameof(items));

        Capacity = capacity;
    }

    public double EvaluateFitness(Chromosome chromosome)
    {
        ArgumentNullException.ThrowIfNull(chromosome);
        if (chromosome.Length != Items.Count)
            throw new ArgumentException(
                $"Chromosome length ({chromosome.Length}) must match number of items ({Items.Count})"
            );

        int totalWeight = 0;
        int totalValue = 0;

        for (int i = 0; i < Items.Count; i++)
        {
            if (chromosome[i])
            {
                totalWeight += Items[i].Weight;
                totalValue += Items[i].Value;
            }
        }

        if (totalWeight > Capacity)
            return -1;

        return totalValue;
    }

    public double CalculateUpperBound()
    {
        if (Items == null || Items.Count == 0 || Capacity <= 0)
            return 0.0;

        var itemsWithDensity = Items
            .Select(
                (item, index) =>
                    new
                    {
                        Item = item,
                        Density = (item.Weight > 0)
                            ? (double)item.Value / item.Weight
                            : (item.Value > 0 ? double.MaxValue : 0),
                    }
            )
            .OrderByDescending(x => x.Density)
            .ToList();

        double totalValueBound = 0;
        int currentWeight = 0;

        foreach (var entry in itemsWithDensity)
        {
            if (currentWeight + entry.Item.Weight <= Capacity)
            {
                totalValueBound += entry.Item.Value;
                currentWeight += entry.Item.Weight;
            }
            else
            {
                int remainingCapacity = Capacity - currentWeight;
                if (remainingCapacity > 0 && entry.Item.Weight > 0)
                {
                    double fraction = (double)remainingCapacity / entry.Item.Weight;
                    totalValueBound += fraction * entry.Item.Value;
                }
                break;
            }
        }
        return totalValueBound;
    }

    public bool CheckChromosomeValidity(Chromosome chromosome)
    {
        if (chromosome == null)
        {
            throw new ArgumentNullException(nameof(chromosome), "Chromosome cannot be null.");
        }
        var totalWeight = 0;
        var totalValue = 0;
        for (int i = 0; i < chromosome.Length; i++)
        {
            if (chromosome[i])
            {
                totalWeight += Items[i].Weight;
                totalValue += Items[i].Value;
            }
        }
        return totalWeight <= Capacity && totalValue > 0;
    }
}
