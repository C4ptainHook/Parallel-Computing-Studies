package tools;


import java.util.HashSet;
import java.util.Set;

public class CommonWords {
    private final Set<String> commonWords;

    public CommonWords() {
        this.commonWords = new HashSet<>();
    }

    public void addCommonWord(String word) {
        this.commonWords.add(word);
    }

    public void combine(CommonWords other) {
        this.commonWords.retainAll(other.commonWords);
    }

    public void addCommonWords(CommonWords other) {
        this.commonWords.addAll(other.commonWords);
    }

    public int getCount() {
        return commonWords.size();
    }

    @Override
    public String toString() {
        return "Common words count: " + getCount() + ", Words: " + commonWords.toString();

    }
}