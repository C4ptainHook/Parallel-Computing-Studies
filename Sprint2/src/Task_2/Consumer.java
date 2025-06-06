package Task_2;

import java.util.Random;

public class Consumer<T> implements Runnable {
    private final Drop<T> drop;
    private final T endItem;

    public Consumer(Drop<T> drop, T endItem) {
        this.drop = drop;
        this.endItem = endItem;
    }

    public void run() {
        for (T item = drop.take();
             !item.equals(endItem);
             item = drop.take())
        {
            System.out.format("Received: %s%n", item);
        }
    }
}