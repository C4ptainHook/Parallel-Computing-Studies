package tasks;

import tools.CommonWords;
import tools.Document;
import tools.Folder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class FolderAnalysisTask extends RecursiveTask<CommonWords> {
    private final Folder folder;

    public FolderAnalysisTask(Folder folder) {
        this.folder = folder;
    }

    @Override
    protected CommonWords compute() {
        List<RecursiveTask<CommonWords>> tasks = new ArrayList<>();
        boolean firstDocument = true;

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

        CommonWords result = new CommonWords();

        for (RecursiveTask<CommonWords> task : tasks) {
            CommonWords docStats = task.join();

            if (firstDocument) {
                result = docStats;
                firstDocument = false;
            } else {
                result.combine(docStats);
            }
        }
        
        return result;
    }
}