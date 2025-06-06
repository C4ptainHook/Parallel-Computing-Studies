namespace GeneticAlgorithm.Algorithm.Selections;

public class TournamentSelection : ISelection
{
    private readonly Random _random;

    public TournamentSelection(int size = 2)
    {
        Size = size;
        AllowWinnerCompeteNextTournament = false;
        _random = new Random();
    }

    public TournamentSelection(int size, bool allowWinnerCompeteNextTournament)
    {
        if (Size <= 0)
        {
            throw new InvalidOperationException("Tournament size cannot be zero or less.");
        }
        Size = size;
        AllowWinnerCompeteNextTournament = allowWinnerCompeteNextTournament;
        _random = new Random();
    }

    public int Size { get; set; }

    public bool AllowWinnerCompeteNextTournament { get; set; }

    public IList<Chromosome> Select(IList<Chromosome> population, int number)
    {
        if (population.Count < number)
        {
            throw new InvalidOperationException(
                $"[Selection Error]\n"
                    + $"Cannot select parents from population.\n"
                    + $"  Number to select: {number}\n"
                    + $"  Available chromosomes: {population.Count}"
            );
        }

        if (Size > population.Count)
        {
            Size = population.Count;
        }

        var currentCandidates = population.ToList();
        var selectedParents = new List<Chromosome>();

        while (selectedParents.Count < number)
        {
            var tournamentParticipants = new List<Chromosome>();
            var availableForTournament = new List<Chromosome>(currentCandidates);
            if (availableForTournament.Count < Size && Size > 0)
            {
                tournamentParticipants.AddRange(availableForTournament);
            }
            else
            {
                double totalFitness = currentCandidates.Sum(c => c.Fitness);
                List<double> probabilities =
                [
                    .. availableForTournament.Select(c => c.Fitness / totalFitness),
                ];
                List<double> cumulativeProbabilities = [];
                double cumulativeSum = 0;
                foreach (var prob in probabilities)
                {
                    cumulativeSum += prob;
                    cumulativeProbabilities.Add(cumulativeSum);
                }

                if (cumulativeProbabilities.Count > 0 && cumulativeProbabilities.Last() < 1.0)
                {
                    cumulativeProbabilities[^1] = 1.0;
                }

                for (int i = 0; i < Size; i++)
                {
                    double randomPick = _random.NextDouble();
                    int selectedIndex = -1;
                    for (int j = 0; j < cumulativeProbabilities.Count; j++)
                    {
                        if (randomPick <= cumulativeProbabilities[j])
                        {
                            selectedIndex = j;
                            break;
                        }
                    }

                    if (selectedIndex == -1 && availableForTournament.Count > 0)
                    {
                        selectedIndex = availableForTournament.Count - 1;
                    }
                    tournamentParticipants.Add(availableForTournament[selectedIndex]);
                }
            }

            var tournamentWinner = tournamentParticipants.OrderByDescending(c => c.Fitness).First();
            selectedParents.Add(tournamentWinner.Clone());

            if (!AllowWinnerCompeteNextTournament)
            {
                var winnerInOriginalList = currentCandidates.FirstOrDefault(c =>
                    c.Equals(tournamentWinner)
                );
                currentCandidates.Remove(winnerInOriginalList!);
            }
        }

        return selectedParents;
    }
}
