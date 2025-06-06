using System.Net.Sockets;
using System.Text;

namespace GeneticAlgorithm.Algorithm.Benchmark;

public class BenchmarkResult
{
    #region Problem configuration
    public int ProblemItemCount { get; set; }
    public int ProblemCapacity { get; set; }
    public double ProblemTheoreticalUpperBound { get; set; }
    #endregion

    #region General configuration
    public string GAType { get; set; }
    public int ConfigPopulationSize { get; set; }
    public int ConfigMaxGenerations { get; set; }
    public int ConfigEliteCount { get; set; }
    public string ConfigSelectionType { get; set; }
    public int? ConfigTournamentSize { get; set; }
    public bool? ConfigAllowWinnerCompeteNext { get; set; }
    public string ConfigCrossoverType { get; set; }
    public string ConfigMutationType { get; set; }
    public string ConfigReintegrationType { get; set; }
    public int ConfigRclSize { get; set; }
    public int? ConfigRandomSeed { get; set; }
    #endregion

    #region Parallel congiguration
    public int? ConfigParNumberOfThreads { get; set; }
    public int? ConfigParWorkingMemoryCapacity { get; set; }
    public int? ConfigParAsyncGenCount { get; set; }
    public int? ConfigParEliteCountPercentage { get; set; }
    #endregion

    #region Stats
    public int RunNumber { get; set; }
    public long ExecutionTimeMilliseconds { get; set; }
    public double BestFitnessAchieved { get; set; }
    public double OptimalityRatio { get; set; }
    #endregion

    const string sep = ";";

    public static string GetCsvHeader()
    {
        var sb = new StringBuilder();
        sb.Append($"ProblemItemCount{sep}ProblemCapacity{sep}ProblemTheoreticalUpperBound{sep}");
        sb.Append($"GAType{sep}");
        sb.Append($"ConfigPopulationSize{sep}ConfigMaxGenerations{sep}ConfigEliteCount{sep}");
        sb.Append($"ConfigSelectionType{sep}ConfigTournamentSize{sep}");
        sb.Append($"ConfigCrossoverType{sep}ConfigMutationType{sep}ConfigReintegrationType{sep}");
        sb.Append($"ConfigRclSize{sep}");
        sb.Append(
            $"ConfigParNumberOfThreads{sep}ConfigParWorkingMemoryCapacity{sep}ConfigParAsyncGenCount{sep}ConfigParEliteCountPercentage{sep}"
        );
        sb.Append(
            $"RunNumber{sep}ExecutionTimeMilliseconds{sep}BestFitnessAchieved{sep}OptimalityRatio"
        );
        return sb.ToString();
    }

    public string ToCsvRow()
    {
        var sb = new StringBuilder();
        sb.Append(
            $"{ProblemItemCount}{sep}{ProblemCapacity}{sep}{ProblemTheoreticalUpperBound:F2}{sep}"
        );
        sb.Append($"{GAType}{sep}");
        sb.Append($"{ConfigPopulationSize}{sep}{ConfigMaxGenerations}{sep}{ConfigEliteCount}{sep}");
        sb.Append($"{ConfigSelectionType}{sep}{ConfigTournamentSize?.ToString() ?? ""}{sep}");
        sb.Append(
            $"{ConfigCrossoverType}{sep}{ConfigMutationType}{sep}{ConfigReintegrationType}{sep}"
        );
        sb.Append($"{ConfigRclSize}{sep}");
        sb.Append(
            $"{ConfigParNumberOfThreads?.ToString() ?? ""}{sep}{ConfigParWorkingMemoryCapacity?.ToString() ?? ""}{sep}{ConfigParAsyncGenCount?.ToString() ?? ""}{sep}{ConfigParEliteCountPercentage?.ToString() ?? ""}{sep}"
        );
        sb.Append(
            $"{RunNumber}{sep}{ExecutionTimeMilliseconds}{sep}{BestFitnessAchieved:F2}{sep}{OptimalityRatio:F4}"
        );
        return sb.ToString();
    }
}
