package Task_2;

public class Drop<T> {
    private T message;

    private boolean empty = true;

    public synchronized T take() {
        while (empty) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        empty = true;
        notify();
        return message;
    }

    public synchronized void put(T message) {
        while (!empty) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        empty = false;
        this.message = message;
        notify();
    }
}