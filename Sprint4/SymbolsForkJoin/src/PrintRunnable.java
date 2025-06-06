public class PrintRunnable implements Runnable {
    private final Printer printer;
    private final char c;
    private final int turn;
    private final int number;

    public PrintRunnable(Printer printer, char c, int turn, int number) {
        this.printer = printer;
        this.c = c;
        this.turn = turn;
        this.number = number;
    }

    @Override
    public void run() {
        for (int i = 0; i < number; i++) {
            printer.print(c, turn);
        }
    }
}