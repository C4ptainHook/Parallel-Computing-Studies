package Task5;

public class SynchronizedMethods implements Synchronizable{
    private int x = 0;

    public synchronized void increment() {
        for(int i = 0; i < 100000; i++){
            x++;
        }
    }

    public synchronized void decrement() {
        for(int i = 0; i < 100000; i++){
            x--;
        }
    }

    public synchronized int getX() {
        return x;
    }
}
