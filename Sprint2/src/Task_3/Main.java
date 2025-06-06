package Task_3;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        Journal journal = new Journal(new int[]{5, 5, 5}, 3);

        Thread[] teacherThreads = new Thread[] {
                new Thread(new Teacher(journal), "Lecturer"),
                new Thread(new Teacher(journal), "Assistant #1"),
                new Thread(new Teacher(journal), "Assistant #2"),
                new Thread(new Teacher(journal), "Assistant #3")
        };

        for (Thread thread : teacherThreads) {
            thread.start();
        }

        for (Thread thread : teacherThreads) {
            thread.join();
        }

        journal.print();
    }
}