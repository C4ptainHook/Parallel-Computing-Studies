package Task1_4;

public class BallRunner implements Runnable {
    private final Ball b;
    private static Thread awaitedThread = null;

    public BallRunner(Ball ball){
        b = ball;
    }
    @Override
    public void run(){
        try{
            while(!b.isInPocket()){
                if (b.isToBeAwaited()) {
                    if(awaitedThread == null)
                        awaitedThread = Thread.currentThread();
                }
                else if (awaitedThread != null && awaitedThread.isAlive()) {
                    awaitedThread.join();
                }
                b.move();
                System.out.println("Thread name = "
                        + Thread.currentThread().getName());
                Thread.sleep(5);
            }
        } catch(InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        finally{
            if (b.isToBeAwaited()) {
                awaitedThread = null;
            }
        }
    }
}
