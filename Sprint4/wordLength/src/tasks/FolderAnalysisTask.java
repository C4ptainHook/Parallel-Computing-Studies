package tasks;

import tools.Document;
import tools.Folder;
import tools.WordStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class FolderAnalysisTask extends RecursiveTask<WordStatistics> {
    private final Folder folder;

    public FolderAnalysisTask(Folder folder) {
        this.folder = folder;
    }

    @Override
    protected WordStatistics compute() {
        List<RecursiveTask<WordStatistics>> tasks = new ArrayList<>();

        // Fork tasks for each subfolder
        for (Folder subFolder : folder.getSubFolders()) {
            FolderAnalysisTask task = new FolderAnalysisTask(subFolder);
            tasks.add(task);
            task.fork();
        }
        
        // Fork tasks for each document
        for (Document doc : folder.getDocuments()) {
            DocumentAnalysisTask task = new DocumentAnalysisTask(doc);
            tasks.add(task);
            task.fork();
        }

        WordStatistics result = new WordStatistics();

        for (RecursiveTask<WordStatistics> task : tasks) {
            result.combine(task.join());
        }
        
        return result;
    }
}