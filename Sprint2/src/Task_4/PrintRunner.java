package Task_4;

public class PrintRunner implements Runnable{
    private final Printer printer;
    private final char c;
    private final int turn;
    private final int number;

    public PrintRunner(Printer printer, char c, int turn, int number) {
        this.printer = printer;
        this.c = c;
        this.turn = turn;
        this.number = number;
    }

    public void run() {
        for (int i = 0; i < number; i++) {
            printer.print(c, turn);
        }
    }
}
