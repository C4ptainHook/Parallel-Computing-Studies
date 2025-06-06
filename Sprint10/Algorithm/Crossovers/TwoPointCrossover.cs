using System.Collections;

namespace GeneticAlgorithm.Algorithm.Crossovers;

public class TwoPointCrossover : ICrossover
{
    public Chromosome Cross(Chromosome leftParent, Chromosome rightParent)
    {
        var firstCutGenesCount = leftParent.Length / 3 + 1;
        var secondCutGenesCount = leftParent.Length / 3 * 2 + 1;
        var child = leftParent.Clone();
        child.ReplaceGenes(
            0,
            new BitArray(leftParent.GetGenes().Take(firstCutGenesCount).ToArray())
        );
        child.ReplaceGenes(
            firstCutGenesCount,
            new BitArray(
                rightParent
                    .GetGenes()
                    .Skip(firstCutGenesCount)
                    .Take(secondCutGenesCount - firstCutGenesCount)
                    .ToArray()
            )
        );
        child.ReplaceGenes(
            secondCutGenesCount,
            new BitArray(leftParent.GetGenes().Skip(secondCutGenesCount).ToArray())
        );
        return child;
    }
}
