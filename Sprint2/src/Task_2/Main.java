package Task_2;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter data length: ");
        final int length = scanner.nextInt();
        scanner.close();

        Drop<Integer> drop = new Drop<>();
        Integer[] data = new Integer[length];
        for (int i = 0; i < length; i++) {
            data[i] = i;
        }
        Integer endItem = -1;
        (new Thread(new Producer<>(drop, data, endItem))).start();
        (new Thread(new Consumer<>(drop, endItem))).start();
    }
}
