import tasks.FolderAnalysisTask;
import tools.Document;
import tools.Folder;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class TextAnalyzer {
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private final Set<String> keywords;

    TextAnalyzer(Set<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> analyzeParallelClassic(Folder folder) {
        return forkJoinPool.invoke(new FolderAnalysisTask(folder, keywords));
    }

    public List<String> analyzeSequential(Folder folder) {
        List<String> foundFiles = new ArrayList<>();

        for (Document doc : folder.getDocuments()) {
            if (containsKeywords(doc)) {
                foundFiles.add(doc.getPath());
            }
        }

        for (Folder subFolder : folder.getSubFolders()) {
            foundFiles.addAll(analyzeSequential(subFolder));
        }

        return foundFiles;
    }


    public boolean containsKeywords(Document document) {

        for (String line : document.lines()) {
            String[] words = line.trim().split("(\\s|\\p{Punct})+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    if (keywords.contains(word.toLowerCase())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}