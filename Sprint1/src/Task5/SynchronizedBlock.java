package Task5;

public class SynchronizedBlock implements Synchronizable{
    private int x = 0;

    public void increment() {
        for(int i = 0; i < 100000; i++){
            synchronized(this) {
                x++;
            }
        }
    }

    public void decrement() {
        for(int i = 0; i < 100000; i++){
            synchronized(this) {
                x--;
            }
        }
    }

    public int getX() {
        return x;
    }
}
