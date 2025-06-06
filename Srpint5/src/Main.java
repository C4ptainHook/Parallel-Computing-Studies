import Simulation.SimulationRunner;

public class Main {
    public static void main(String[] args)  {
        double avgArrivalInterval = 0.1;
        double avgServiceTime = 2;
        int observationInterval = 1;

        int numChannels = 5;
        int queueCapacity = 20;
        int numSimulations = 5;

        long simulationTime = 6;

        SimulationRunner simulationRunner = new SimulationRunner(
                numSimulations, numChannels, queueCapacity,
                avgArrivalInterval, avgServiceTime, simulationTime, observationInterval
        );

        simulationRunner.runSimulations();
    }
}