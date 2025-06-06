package Simulation;

public class SimulationResults {
    private final int totalGenerated;
    private final int totalRejected;
    private final double avgQueueLength;

    public SimulationResults(int totalGenerated, int totalRejected, double avgQueueLength) {
        this.totalGenerated = totalGenerated;
        this.totalRejected = totalRejected;
        this.avgQueueLength = avgQueueLength;
    }

    public int getTotalGenerated() {
        return totalGenerated;
    }

    public int getTotalRejected() {
        return totalRejected;
    }

    public double getAvgQueueLength() {
        return avgQueueLength;
    }
}