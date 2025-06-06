package tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Folder {
    private final List<Folder> subFolders;
    private final List<Document> documents;

    public Folder(List<Folder> subFolders, List<Document> documents) {
        this.subFolders = subFolders;
        this.documents = documents;
    }
    
    public List<Folder> getSubFolders () {
        return this.subFolders;
    }
    public List<Document> getDocuments () {
        return this.documents;
    }
    
    public static Folder fromDirectory(File dir) throws IOException {
        List<Folder> subFolders = new ArrayList<>();
        List<Document> documents = new ArrayList<>();
        File[] files = dir.listFiles();
        
        if (files != null) {
            for (File entry : files) {
                if (entry.isDirectory()) {
                    subFolders.add(Folder.fromDirectory(entry));
                } else if (entry.getName().toLowerCase().endsWith(".txt")) {
                    documents.add(Document.fromFile(entry));
                }
            }
        }
        
        return new Folder(subFolders, documents);
    }
}