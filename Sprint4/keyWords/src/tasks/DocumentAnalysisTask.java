package tasks;

import tools.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

public class DocumentAnalysisTask extends RecursiveTask<List<String>> {
    private final Document document;
    private final Set<String> keywords;
    public DocumentAnalysisTask(Document document, Set<String> keywords) {
        this.document = document;
        this.keywords = keywords;
    }
    @Override
    protected List<String> compute () {

        for (String line : document.lines()) {
            String[] words = line.trim().split("(\\s|\\p{Punct})+");

            for (String word : words) {
                if (keywords.contains(word.toLowerCase())) {
                    return new ArrayList<>(List.of(document.getPath()));
                }
            }
        }

        return null;
    }
}