package Simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SimulationRunner {
    private final int numSimulations;
    private final int numChannels;
    private final int queueCapacity;
    private final double avgArrivalInterval;
    private final double avgServiceTime;
    private final long simulationTime;
    private final int observationInterval;

    public SimulationRunner(int numSimulations, int numChannels, int queueCapacity,
                            double avgArrivalInterval, double avgServiceTime, long simulationTime, int observationInterval) {
        this.numSimulations = numSimulations;
        this.numChannels = numChannels;
        this.queueCapacity = queueCapacity;
        this.avgArrivalInterval = avgArrivalInterval;
        this.avgServiceTime = avgServiceTime;
        this.simulationTime = simulationTime;
        this.observationInterval = observationInterval;
    }

    public void runSimulations() {
        ExecutorService executorService = Executors.newFixedThreadPool(numSimulations);
        List<Future<SimulationResults>> futures = new ArrayList<>();

        for (int i = 0; i < numSimulations; i++) {
            QueueSimulation simulation = new QueueSimulation(
                    numChannels, queueCapacity, avgArrivalInterval, avgServiceTime, simulationTime, observationInterval
            );
            var simulationTask = new Callable<SimulationResults>() {
                @Override
                public SimulationResults call() {
                    simulation.runSimulation();
                    simulation.printResults();
                    return simulation.getResults();
                }
            };
            futures.add(executorService.submit(simulationTask));
        }

        int totalGenerated = 0;
        int totalRejected = 0;
        double totalQueueLength = 0;

        for (Future<SimulationResults> future : futures) {
            try {
                SimulationResults result = future.get();
                totalGenerated += result.getTotalGenerated();
                totalRejected += result.getTotalRejected();
                totalQueueLength += result.getAvgQueueLength();
            } catch (ExecutionException e) {
                System.err.println("Error during simulation: " + e.getCause());
            } catch (InterruptedException e) {
                System.err.println("Simulation was interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        executorService.shutdown();

        double avgRejectionProbability = (double) totalRejected / totalGenerated;
        double avgQueueLength = totalQueueLength / numSimulations;

        System.out.println("\n====== SIMULATION SUMMARY ======");
        System.out.println("Average queue length: " + String.format("%.4f", avgQueueLength));
        System.out.println("Average rejection probability: " + String.format("%.4f", avgRejectionProbability));
    }
}