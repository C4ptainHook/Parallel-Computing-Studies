public class Task {
    public static void main(String[] args) {
        State state = new State();

        Thread threadA = new Thread(new StateChanger(state));
        Thread threadB = new Thread(new Counter(state));

        threadA.start();
        threadB.start();
    }

    static class State {
        private volatile String state = "r";

        public synchronized String getState() {
            return state;
        }

        public synchronized void setState(String state) {
            this.state = state;
            notifyAll();
        }
    }

    static class StateChanger implements Runnable {
        private final State state;

        public StateChanger(State state) {
            this.state = state;
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if ("r".equals(state.getState())) {
                        Thread.sleep(100);
                        state.setState("w");
                    } else {
                        Thread.sleep(100);
                        state.setState("r");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Counter implements Runnable {
        private final State state;

        public Counter(State state) {
            this.state = state;
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    synchronized (state) {
                        while (!"r".equals(state.getState())) {
                            state.wait();
                        }

                        for (int i = 100; i >= 0; i--) {
                            System.out.println(i);
                            Thread.sleep(1);

                            if (!"r".equals(state.getState())) {
                                break;
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}