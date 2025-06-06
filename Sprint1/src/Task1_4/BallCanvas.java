package Task1_4;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BallCanvas extends JPanel {
    private ArrayList<Ball> balls = new ArrayList<>();
    private Lock lock = new ReentrantLock();
    private int ballsInPocket = 0;
    public void add(Ball b){
        this.balls.add(b);
    }
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;

        int pocketSize = 25;
        g2.setColor(Color.BLACK);
        g2.fill(new Ellipse2D.Double(0, 0, pocketSize, pocketSize));
        g2.fill(new Ellipse2D.Double(this.getWidth() - pocketSize, 0, pocketSize, pocketSize));
        g2.fill(new Ellipse2D.Double(0, this.getHeight() - pocketSize, pocketSize, pocketSize));
        g2.fill(new Ellipse2D.Double(this.getWidth() - pocketSize, this.getHeight() - pocketSize, pocketSize, pocketSize));

        for (int i = balls.size() - 1; i >= 0; i--) {
            Ball b = balls.get(i);
            if (b.isInPocket()) {
                balls.remove(i);
                lock.lock();
                ballsInPocket++;
                lock.unlock();
            } else {
                b.draw(g2);
            }
        }
    }

    public int getBallsInPocket() {
        return ballsInPocket;
    }
}