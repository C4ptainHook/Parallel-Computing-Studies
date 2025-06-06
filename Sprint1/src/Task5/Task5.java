package Task5;

public class Task5 {
    public static void main(String[] args) {
        var unsynchronized = new Unsynchronized();
        var synchronizedMethods = new SynchronizedMethods();
        var synchronizedBlock = new SynchronizedBlock();
        var synchronizedLock = new SynchronizedLock();

        var result1 = RunFunction(unsynchronized);
        var result2 = RunFunction(synchronizedMethods);
        var result3 = RunFunction(synchronizedBlock);
        var result4 = RunFunction(synchronizedLock);

        System.out.println("Non-synchronized final value = " + result1);
        System.out.println("Synchronized methods final value = " + result2);
        System.out.println("Synchronized blocks final value = " + result3);
        System.out.println("Synchronized with locks final value = " + result4);
    }

    private static int RunFunction(Synchronizable s)
    {
        var unsynchronized = new Unsynchronized();
        Thread thread1 = new Thread(s::increment);
        Thread thread2 = new Thread(s::decrement);
        thread1.start();
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return s.getX();
    }
}
