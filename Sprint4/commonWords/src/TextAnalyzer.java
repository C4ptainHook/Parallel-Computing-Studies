import tasks.FolderAnalysisTask;
import tools.CommonWords;
import tools.Document;
import tools.Folder;


import java.util.concurrent.ForkJoinPool;

public class TextAnalyzer {
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    public CommonWords analyzeParallelClassic(Folder folder) {
        return forkJoinPool.invoke(new FolderAnalysisTask(folder));
    }

    public CommonWords analyzeSequential(Folder folder) {
        CommonWords commonWords = getCommonWords();

        boolean firstDocument = true;

        for (Document doc : folder.getDocuments()) {
            CommonWords docStats = analyzeDocument(doc);

            if (firstDocument) {
                commonWords = docStats;
                firstDocument = false;
            } else {
                commonWords.combine(docStats);
            }
        }

        for (Folder subFolder : folder.getSubFolders()) {
            commonWords.combine(analyzeSequential(subFolder));
        }

        return commonWords;
    }
    
    private CommonWords getCommonWords () {
        return new CommonWords();
    }

    public CommonWords analyzeDocument(Document document) {
        CommonWords commonWords = getCommonWords();

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