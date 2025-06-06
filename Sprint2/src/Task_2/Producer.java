package Task_2;

import java.util.Random;

public class Producer<T> implements Runnable {
    private final Drop<T> drop;
    private final T[] data;
    private final T endItem;

    public Producer(Drop<T> drop, T[] data, T endItem) {
        this.drop = drop;
        this.data = data;
        this.endItem = endItem;
    }

    public void run() {
        Random random = new Random();

        for (T item : data) {
            drop.put(item);
            try {
                Thread.sleep(random.nextInt(1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        drop.put(endItem);
    }
}