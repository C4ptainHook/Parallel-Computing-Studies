package Simulation;

import Queue.LimitedQueue;
import Queue.QueueObserver;
import Request.RequestProducer;
import Channel.RequestChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class QueueSimulation {
    private final int numChannels;
    private final int queueCapacity;
    private final double avgArrivalInterval;
    private final double avgServiceTime;
    private final long simulationTime;
    private final int observationInterval;

    private LimitedQueue queue;
    private RequestProducer requestProducer;
    private List<RequestChannel> requestChannels;
    private QueueObserver queueObserver;

    public QueueSimulation(int numChannels, int queueCapacity, double avgArrivalInterval,
                           double avgServiceTime, long simulationTime, int observationInterval) {
        this.numChannels = numChannels;
        this.queueCapacity = queueCapacity;
        this.avgArrivalInterval = avgArrivalInterval;
        this.avgServiceTime = avgServiceTime;
        this.simulationTime = simulationTime;
        this.observationInterval = observationInterval;
    }

    public void runSimulation() {
        System.out.println("Starting simulation...");
        System.out.println("PARAMETERS:");
        System.out.println("Channels: " + numChannels);
        System.out.println("Queue capacity: " + queueCapacity);
        System.out.println("Avg service time: " + avgServiceTime + " seconds");
        System.out.println("Avg arrival interval: " + avgArrivalInterval + " seconds");
        System.out.println("Simulation time: " + simulationTime + " seconds");

        queue = new LimitedQueue(queueCapacity);
        CountDownLatch tracker = new CountDownLatch(numChannels + 1); // +1 for generator
        requestProducer = new RequestProducer(queue, avgArrivalInterval, simulationTime, tracker);
        Thread generatorThread = new Thread(requestProducer);

        requestChannels = new ArrayList<>();
        List<Thread> channelThreads = new ArrayList<>();

        for (int i = 0; i < numChannels; i++) {
            RequestChannel channel = new RequestChannel(i + 1, queue, avgServiceTime, tracker, simulationTime);
            requestChannels.add(channel);
            Thread channelThread = new Thread(channel);
            channelThreads.add(channelThread);
        }

        queueObserver = new QueueObserver(queue, requestProducer, observationInterval);
        Thread observerThread = new Thread(queueObserver);

        generatorThread.start();
        for (Thread thread : channelThreads) {
            thread.start();
        }
        observerThread.start();

        try {
            tracker.await();
            queueObserver.stop();
            observerThread.join();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Simulation interrupted: " + e.getMessage());
        }

        System.out.println("Simulation completed.");
    }

    public void printResults() {
        int totalServed = requestChannels.stream()
                .mapToInt(RequestChannel::getRequestsProcessed)
                .sum();

        int totalGenerated = requestProducer.getRequestGenerated();
        int totalRejected = requestProducer.getRequestsRejected();

        double rejectionProbability = (double) totalRejected / totalGenerated;

        double avgQueueLength = queueObserver.getAverageQueueLength();

        System.out.println("\n");
        System.out.println("Total requests generated: " + totalGenerated);
        System.out.println("Total requests served: " + totalServed);
        System.out.println("Total requests rejected: " + totalRejected);
        System.out.println("Rejection probability: " + String.format("%.4f", rejectionProbability));
        System.out.println("Average queue length: " + String.format("%.2f", avgQueueLength));

        System.out.println("\nPer-channel statistics:");
        for (int i = 0; i < requestChannels.size(); i++) {
            RequestChannel channel = requestChannels.get(i);
            System.out.println("Channel " + (i + 1) + " served " + channel.getRequestsProcessed() + " requests");
        }
    }

    public SimulationResults getResults() {
        int totalGenerated = requestProducer.getRequestGenerated();
        int totalRejected = requestProducer.getRequestsRejected();
        double avgQueueLength = queueObserver.getAverageQueueLength();
        return new SimulationResults(totalGenerated, totalRejected, avgQueueLength);
    }
}