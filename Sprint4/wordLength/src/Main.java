import tools.Folder;
import tools.WordStatistics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        String folderPath = Paths.get("wordLength/files").toAbsolutePath().toString();

        File folderFile = new File(folderPath);
        if (!folderFile.isDirectory()) {
            System.err.println("Error: " + folderPath + " is not a directory.");
            System.exit(1);
        }
        try {
            Folder folder = Folder.fromDirectory(folderFile);
            TextAnalyzer analyzer = new TextAnalyzer();

            // Sequential analysis
            long startSeq = System.currentTimeMillis();
            WordStatistics seqStats = analyzer.analyzeSequential(folder);
            long endSeq = System.currentTimeMillis();
            long timeSeq = endSeq - startSeq;

            // Classical Fork/Join analysis
            long startParClassic = System.currentTimeMillis();
            WordStatistics parClassicStats = analyzer.analyzeParallelClassic(folder);
            long endParClassic = System.currentTimeMillis();
            long timeParClassic = endParClassic - startParClassic;

            System.out.println("= Sequential Analysis =");
            System.out.println(seqStats);
            System.out.println("Elapsed time (ms): " + timeSeq);
            System.out.println();

            System.out.println("= Parallel Analysis =");
            System.out.println(parClassicStats);
            System.out.println("Elapsed time (ms): " + timeParClassic);
            System.out.println();

            if (timeParClassic > 0) {
                double speedupSeqClassic = (double) timeSeq / timeParClassic;
                System.out.printf("Speedup (Sequential / Classical ForkJoin): %.2f\n", speedupSeqClassic);
            }

        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
        }
    }
}