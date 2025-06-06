public class Printer {
    private final int turnsNumber;
    private final int lineLength;
    private int currentTurn = 0;

    public Printer(int turnsNumber, int lineLength) {
        this.turnsNumber = turnsNumber;
        this.lineLength = lineLength;
    }

    public synchronized void print(char c, int turn) {
        while (turn != (currentTurn % turnsNumber)) {
            try {
                wait();
            } catch (InterruptedException e) {
                return;
            }
        }

        currentTurn++;

        System.out.print(c);
        if (currentTurn % lineLength == 0) {
            System.out.println();
        }
        notifyAll();
    }
}