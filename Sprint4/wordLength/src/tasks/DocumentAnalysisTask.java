package tasks;

import tools.Document;
import tools.WordStatistics;

import java.util.concurrent.RecursiveTask;

public class DocumentAnalysisTask extends RecursiveTask<WordStatistics> {
    private final Document document;
    public DocumentAnalysisTask(Document document) {
        this.document = document;
    }
    @Override
    protected WordStatistics compute () {
        WordStatistics stats = new WordStatistics();

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