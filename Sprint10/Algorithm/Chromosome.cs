using System.Collections;

namespace GeneticAlgorithm.Algorithm;

public class Chromosome : IEquatable<Chromosome>
{
    private readonly BitArray _genes;
    public double Fitness { get; set; } = -1;
    public int Length => _genes.Length;

    public Chromosome(int length)
    {
        if (length <= 0)
        {
            throw new ArgumentOutOfRangeException(
                nameof(length),
                "Length must be greater than zero."
            );
        }
        _genes = new BitArray(length);
    }

    public Chromosome(BitArray genes)
    {
        if (genes == null)
        {
            throw new ArgumentNullException(nameof(genes), "Genes cannot be null.");
        }
        _genes = new BitArray(genes);
    }

    public void FlipGene(int index)
    {
        this[index] = !this[index];
    }

    public IEnumerable<bool> GetGenes()
    {
        for (int i = 0; i < Length; i++)
        {
            yield return _genes[i];
        }
    }

    public bool this[int index]
    {
        get
        {
            if (index < 0 || index >= _genes.Length)
            {
                throw new ArgumentOutOfRangeException(
                    nameof(index),
                    $"Cannot get gene at index {index}. Index is out of range."
                );
            }
            return _genes[index];
        }
        set
        {
            if (index < 0 || index >= _genes.Length)
            {
                throw new ArgumentOutOfRangeException(
                    nameof(index),
                    $"Cannot set gene at index {index}. Index is out of range."
                );
            }
            _genes[index] = value;
        }
    }

    public void ReplaceGenes(int startIndex, BitArray genesToInsert)
    {
        if (genesToInsert.Length > 0)
        {
            if (startIndex < 0 || startIndex >= Length)
            {
                throw new ArgumentOutOfRangeException(
                    nameof(startIndex),
                    $"Cannot replace genes at index {startIndex}"
                );
            }

            int copyLength = Math.Min(genesToInsert.Length, Length - startIndex);
            for (int i = 0; i < copyLength; i++)
            {
                _genes[startIndex + i] = genesToInsert[i];
            }

            Fitness = -1;
        }
    }

    public Chromosome Clone()
    {
        return new Chromosome(new BitArray(_genes)) { Fitness = this.Fitness };
    }

    public bool Equals(Chromosome? other)
    {
        if (other == null)
        {
            return false;
        }

        if (ReferenceEquals(this, other))
        {
            return true;
        }

        if (Fitness != other.Fitness)
        {
            return false;
        }

        if (Length != other.Length)
        {
            return false;
        }

        for (int i = 0; i < Length; i++)
        {
            if (_genes[i] != other._genes[i])
            {
                return false;
            }
        }

        return true;
    }
}
