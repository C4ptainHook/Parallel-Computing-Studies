package tasks;

import tools.CommonWords;
import tools.Document;

import java.util.concurrent.RecursiveTask;

public class DocumentAnalysisTask extends RecursiveTask<CommonWords> {
    private final Document document;
    public DocumentAnalysisTask(Document document) {
        this.document = document;
    }
    @Override
    protected CommonWords compute () {
        CommonWords commonWords = new CommonWords();

        for (String line : document.lines()) {
            String[] words = line.trim().split("(\\s|\\p{Punct})+");

            for (String word : words) {
                if (!word.isEmpty()) {
                    commonWords.addCommonWord(word);
                }
            }
        }

        return commonWords;
    }
}