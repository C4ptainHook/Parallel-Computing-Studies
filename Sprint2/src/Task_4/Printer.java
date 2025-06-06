package Task_4;

public class Printer {
    private final int numberOfTurns;
    private final int lineLength;
    private int currentTurn = 0;

    public Printer(int nOfTurns, int lineLength) {
        this.numberOfTurns = nOfTurns;
        this.lineLength = lineLength;
    }

    public synchronized void print(char c, int turn){
        while (turn != (currentTurn % numberOfTurns)){
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        currentTurn++;

        System.out.print(c);
        if (currentTurn % lineLength == 0){
            System.out.println();
        }
        notifyAll();
    }
}
