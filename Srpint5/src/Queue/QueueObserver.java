package Queue;

import Request.RequestProducer;

import java.util.ArrayList;
import java.util.List;

public class QueueObserver implements Runnable {
    private final LimitedQueue queue;
    private final RequestProducer requestProducer;
    private final int observationInterval;
    private final List<Integer> queueLengthObservations = new ArrayList<>();
    private volatile boolean running = true;

    public QueueObserver(LimitedQueue queue, RequestProducer requestProducer, int observationInterval) {
        this.queue = queue;
        this.requestProducer = requestProducer;
        this.observationInterval = observationInterval;
    }

    @Override
    public void run() {
        try {
            while (running) {
                int currentLength = queue.size();
                queueLengthObservations.add(currentLength);
                int requestsGenerated = requestProducer.getRequestGenerated();
                int requestsRejected = requestProducer.getRequestsRejected();
                int requestsProcessed = requestsGenerated - requestsRejected;

                System.out.println("Queue observation: length = " + currentLength);
                System.out.println("Processed requests: " + requestsProcessed + ", Rejected requests: " + requestsRejected);
                Thread.sleep(observationInterval * 1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public void stop() {
        running = false;
    }

    public double getAverageQueueLength() {
        if (queueLengthObservations.isEmpty()) {
            return 0.0;
        }
        return queueLengthObservations.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }
}