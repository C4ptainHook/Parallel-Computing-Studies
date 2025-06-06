namespace GeneticAlgorithm.Problem;

public class Item
{
    public int Weight { get; }
    public int Value { get; }

    public Item(int weight, int value)
    {
        if (weight <= 0)
            throw new ArgumentException("Weight must be greater than zero", nameof(weight));
        if (value <= 0)
            throw new ArgumentException("Value must be greater than zero", nameof(value));

        Weight = weight;
        Value = value;
    }
}
