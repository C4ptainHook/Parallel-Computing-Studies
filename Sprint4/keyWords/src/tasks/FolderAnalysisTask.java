package tasks;

import tools.Document;
import tools.Folder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

public class FolderAnalysisTask extends RecursiveTask<List<String>> {
    private final Folder folder;
    private final Set<String> keywords;

    public FolderAnalysisTask(Folder folder, Set<String> keywords) {
        this.folder = folder;
        this.keywords = keywords;
    }

    @Override
    protected List<String> compute() {
        List<RecursiveTask<List<String>>> tasks = new ArrayList<>();

        for (Folder subFolder : folder.getSubFolders()) {
            FolderAnalysisTask task = new FolderAnalysisTask(subFolder, keywords);
            tasks.add(task);
            task.fork();
        }

        for (Document doc : folder.getDocuments()) {
            DocumentAnalysisTask task = new DocumentAnalysisTask(doc, keywords);
            tasks.add(task);
            task.fork();
        }


        List<String> foundFiles = new ArrayList<>();

        for (RecursiveTask<List<String>> task : tasks) {
            List<String> result = task.join();
            if (result != null) {
                foundFiles.addAll(result);
            }
        }

        return foundFiles;
    }
}