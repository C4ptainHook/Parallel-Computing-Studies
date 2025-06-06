namespace GeneticAlgorithm.Problem;

public class KnapsackProblemGenerator(int? seed = null)
{
    private readonly Random _random = seed.HasValue ? new Random(seed.Value) : new Random();

    public KnapsackProblem Generate(
        int itemCount,
        int minWeight = 1,
        int maxWeight = 100,
        int minValue = 1,
        int maxValue = 100,
        double capacityRatio = 0.5
    )
    {
        if (itemCount <= 0)
            throw new ArgumentException("Item count must be greater than zero", nameof(itemCount));
        if (minWeight <= 0 || maxWeight <= 0 || minWeight > maxWeight)
            throw new ArgumentException("Invalid weight range");
        if (minValue <= 0 || maxValue <= 0 || minValue > maxValue)
            throw new ArgumentException("Invalid value range");
        if (capacityRatio <= 0 || capacityRatio > 1)
            throw new ArgumentException(
                "Capacity ratio must be between 0 and 1",
                nameof(capacityRatio)
            );

        var items = new List<Item>(itemCount);
        int totalWeight = 0;

        for (int i = 0; i < itemCount; i++)
        {
            int weight = _random.Next(minWeight, maxWeight + 1);
            int value = _random.Next(minValue, maxValue + 1);
            items.Add(new Item(weight, value));
            totalWeight += weight;
        }

        int capacity = (int)(totalWeight * capacityRatio);

        return new KnapsackProblem(items, capacity);
    }
}
