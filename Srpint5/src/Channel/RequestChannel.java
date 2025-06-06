package Channel;

import Request.Request;
import Queue.LimitedQueue;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestChannel implements Runnable {
    private final int channelId;
    private final LimitedQueue queue;
    private final Random random = new Random();
    private final AtomicInteger requestProcessed = new AtomicInteger(0);
    private final CountDownLatch tracker;

    //Simulation parameters
    private final double expectedProcessingTime;
    private final long simulationDuration;

    public RequestChannel(int channelId, LimitedQueue queue,
                          double expectedProcessingTime, CountDownLatch tracker,
                          long simulationDuration) {
        this.channelId = channelId;
        this.queue = queue;
        this.expectedProcessingTime = expectedProcessingTime;
        this.simulationDuration = simulationDuration;
        this.tracker = tracker;
    }

    @Override
    public void run() {
        long endTime = System.currentTimeMillis() + simulationDuration * 1000;
        try {
            while (System.currentTimeMillis() < endTime) {
                Request request = queue.fetchRequest(1, TimeUnit.SECONDS);

                if (request != null) {
                    double serviceTime = Math.max(0.5, random.nextGaussian() * (expectedProcessingTime /3) + expectedProcessingTime);
                    System.out.println("Channel " + channelId + " processing request " + request.id() +
                            " for " + String.format("%.2f", serviceTime) + " seconds");

                    Thread.sleep((long) (serviceTime * 1000));
                    requestProcessed.incrementAndGet();

                    System.out.println("Channel " + channelId + " finished processing request " + request.id());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            tracker.countDown();
            System.out.println("Service channel " + channelId + " finished. Served: " + requestProcessed.get());
        }
    }
    public int getRequestsProcessed() {
        return requestProcessed.get();
    }
}