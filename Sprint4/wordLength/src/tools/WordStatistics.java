package tools;


public class WordStatistics {
    long count;
    long sumLengths;
    long sumSquaredLengths;
    int minLength;
    int maxLength;
    
    public WordStatistics() {
        count = 0;
        sumLengths = 0;
        sumSquaredLengths = 0;
        minLength = Integer.MAX_VALUE;
        maxLength = Integer.MIN_VALUE;
    }

    public void addWordLength (int len) {
        count++;
        sumLengths += len;
        sumSquaredLengths += (long) len * len;
        if (len < minLength) {
            minLength = len;
        }
        if (len > maxLength) {
            maxLength = len;
        }
    }


    
    public void combine (WordStatistics other) {
        count += other.count;
        sumLengths += other.sumLengths;
        sumSquaredLengths += other.sumSquaredLengths;
        if (other.count > 0) {
            minLength = Math.min(this.minLength, other.minLength);
            maxLength = Math.max(this.maxLength, other.maxLength);
        }
    }
    
    public double getAverage () {
        return count == 0 ? 0 : (double) sumLengths / (double) count;
    }
    
    public double getVariance () {
        if (count == 0) {
            return 0;
        }

        double avg = getAverage();

        return (double) sumSquaredLengths / (double) count - avg * avg;
    }

    @Override
    public String toString() {
        return String.format(
                "Words: %d, Avg Length: %.2f, Variance: %.2f, Min Length: %d, Max Length: %d",
                count, getAverage(), getVariance(), minLength, maxLength
        );
    }
}