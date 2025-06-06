package Task1_4;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Random;

class Ball {
    private final Component canvas;
    private static final int XSIZE = 20;
    private static final int YSIZE = 20;
    private int x = 0;
    private int y= 0;
    private int dx = 2;
    private int dy = 2;
    private final BallColor color;
    private final BallColor colorToWaitFor = BallColor.BLUE;


    public Ball(Component c, BallColor color, SpawnPoint spawnPoint){
        this.canvas = c;
        this.color = color;

        switch (spawnPoint){
            case FIXED:
                x = this.canvas.getHeight() / 2;
                y = 0;
                break;
            case RANDOM:
                if(Math.random()<0.5){
                    x = new Random().nextInt(this.canvas.getWidth());
                    y = 0;
                }else{
                    x = 0;
                    y = new Random().nextInt(this.canvas.getHeight());
                }
        }
    }

    public boolean isToBeAwaited(){
        return this.color == colorToWaitFor;
    }

    public void draw (Graphics2D g2){
        switch (color){
            case RED:
                g2.setColor(Color.RED);
                break;
            case YELLOW:
                g2.setColor(Color.YELLOW);
                break;
            case BLUE:
                g2.setColor(Color.BLUE);
                break;
        }

        g2.fill(new Ellipse2D.Double(x,y,XSIZE,YSIZE));
    }

    public void move(){
        x+=dx;
        y+=dy;
        if(x<0){
            x = 0;
            dx = -dx;
        }
        if(x+XSIZE>=this.canvas.getWidth()){
            x = this.canvas.getWidth()-XSIZE;
            dx = -dx;
        }
        if(y<0){
            y=0;
            dy = -dy;
        }
        if(y+YSIZE>=this.canvas.getHeight()){
            y = this.canvas.getHeight()-YSIZE;
            dy = -dy;
        }
        this.canvas.repaint();
    }

    public boolean isInPocket() {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        int pocketSizeX = 2 * XSIZE;
        int pocketSizeY = 2 * YSIZE;

        return (x < pocketSizeX && y < pocketSizeY) ||
                (x > width - pocketSizeX && y < pocketSizeY) ||
                (x < pocketSizeX && y > height - pocketSizeY) ||
                (x > width - pocketSizeX && y > height - pocketSizeY);
    }

    public BallColor getColor() {
        return color;
    }
}
