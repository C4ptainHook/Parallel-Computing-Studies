import tasks.FolderAnalysisTask;
import tools.Document;
import tools.Folder;
import tools.WordStatistics;

import java.util.concurrent.ForkJoinPool;

public class TextAnalyzer {
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    public WordStatistics analyzeParallelClassic(Folder folder) {
        return forkJoinPool.invoke(new FolderAnalysisTask(folder));
    }

    public WordStatistics analyzeSequential(Folder folder) {
        WordStatistics stats = getWordStatistic();

        for (Document doc : folder.getDocuments()) {
            stats.combine(analyzeDocument(doc));
        }

        for (Folder subFolder : folder.getSubFolders()) {
            stats.combine(analyzeSequential(subFolder));
        }

        return stats;
    }
    
    private WordStatistics getWordStatistic () {
        return new WordStatistics();
    }

    public WordStatistics analyzeDocument(Document document) {
        WordStatistics stats = getWordStatistic();

        for (String line : document.lines()) {
            String[] words = line.trim().split("(\\s|\\p{Punct})+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    stats.addWordLength(word.length());
                }
            }
        }

        return stats;
    }
}