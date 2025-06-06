package Request;

import Queue.LimitedQueue;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestProducer implements Runnable {
    private final LimitedQueue queue;
    private final Random random = new Random();
    private final AtomicInteger requestGenerated = new AtomicInteger(0);
    private final AtomicInteger requestsRejected = new AtomicInteger(0);
    private final CountDownLatch tracker;

    //Simulation parameters
    private final double avgArrivalInterval;
    private final long simulationTime;

    public RequestProducer(LimitedQueue queue, double avgArrivalInterval, long simulationTime, CountDownLatch tracker) {
        this.queue = queue;
        this.avgArrivalInterval = avgArrivalInterval;
        this.simulationTime = simulationTime;
        this.tracker = tracker;
    }

    @Override
    public void run() {
        long endTime = System.currentTimeMillis() + simulationTime * 1000;

        try {
            while (System.currentTimeMillis() < endTime) {
                int requestId = requestGenerated.incrementAndGet();
                Request request = new Request(requestId, System.currentTimeMillis());
                boolean added = queue.tryAddRequest(request);
                if (!added) {
                    requestsRejected.incrementAndGet();
                    System.out.println("Request " + requestId + " rejected - queue full");
                } else {
                    System.out.println("Request " + requestId + " added to queue");
                }

                double arrivalInterval = random.nextDouble() * (2 * avgArrivalInterval);
                Thread.sleep((long) (arrivalInterval * 1000));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            tracker.countDown();
        }
    }

    public int getRequestGenerated() {
        return requestGenerated.get();
    }

    public int getRequestsRejected() {
        return requestsRejected.get();
    }
}