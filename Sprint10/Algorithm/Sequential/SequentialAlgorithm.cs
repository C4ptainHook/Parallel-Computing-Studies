using GeneticAlgorithm.Problem;

namespace GeneticAlgorithm.Algorithm.Sequential;

public class SequentialAlgorithm(
    KnapsackProblem problem,
    GeneticAlgorithmConfiguration configuration
) : BaseGeneticAlgorithm<GeneticAlgorithmConfiguration>(problem, configuration)
{
    public override Chromosome Run()
    {
        var population = CreateInitialPopulation();
        foreach (var chromosome in population)
        {
            chromosome.Fitness = Problem.EvaluateFitness(chromosome);
        }
        while (CurrentGeneration < Configuration.MaxGenerations)
        {
            var offsprings = new List<Chromosome>();
            foreach (var parent in population)
            {
                var parents = Configuration.Selection.Select(population, 2);
                var left_child = Configuration.Crossover.Cross(parents[0], parents[1]);
                var right_child = Configuration.Crossover.Cross(parents[1], parents[0]);
                var mutated_left_child = Configuration.Mutation.Mutate(
                    left_child,
                    1.0 / problem.Items.Count
                );
                var mutated_right_child = Configuration.Mutation.Mutate(
                    right_child,
                    1.0 / problem.Items.Count
                );
                mutated_left_child.Fitness = Problem.EvaluateFitness(mutated_left_child);
                mutated_right_child.Fitness = Problem.EvaluateFitness(mutated_right_child);
                offsprings.Add(mutated_left_child);
                offsprings.Add(mutated_right_child);
            }
            population = Configuration.Reinsertion.Reintegrate(
                population,
                offsprings,
                Configuration.EliteCount
            );
            var invalidSolutions = population.Where(c => c.Fitness <= 0).ToList();
            CurrentGeneration++;
        }
        var bestChromosome = population.OrderByDescending(c => c.Fitness).First();
        BestFitness = bestChromosome.Fitness;
        return bestChromosome;
    }
}
