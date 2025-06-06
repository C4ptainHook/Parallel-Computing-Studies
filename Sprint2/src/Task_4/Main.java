package Task_4;

public class Main {
    private static final int LINE_LENGTH = 30;
    private static final int LINES_NUMBER = 90;

    public static void main(String[] args) {
        Printer printer = new Printer(3, LINE_LENGTH);

        int totalChars = LINE_LENGTH * LINES_NUMBER;

        (new Thread(new PrintRunner(printer,'|', 0, totalChars))).start();
        (new Thread(new PrintRunner(printer, '\\', 1, totalChars))).start();
        (new Thread(new PrintRunner(printer, '/', 2, totalChars))).start();
    }
}