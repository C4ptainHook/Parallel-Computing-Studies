import java.util.concurrent.RecursiveAction;

public class PrintTask extends RecursiveAction {
    private final Printer printer;
    private final char c;
    private final int turn;
    private final int number;

    public PrintTask(Printer printer, char c, int turn, int number) {
        this.printer = printer;
        this.c = c;
        this.turn = turn;
        this.number = number;
    }

    @Override
    protected void compute() {
        for (int i = 0; i < number; i++) {
            printer.print(c, turn);
        }
    }
}