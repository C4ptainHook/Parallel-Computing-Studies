package Queue;

import Request.Request;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class LimitedQueue {
    private final BlockingQueue<Request> queue;

    public LimitedQueue(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    public boolean tryAddRequest(Request request) {
        return queue.offer(request);
    }

    public Request fetchRequest(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    public int size() {
        return queue.size();
    }
}