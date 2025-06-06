package Task1_4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BounceFrame extends JFrame {
    private BallCanvas canvas;
    private JLabel ballCountLabel;

    private JComboBox<BallColor> colorPicker;
    private JComboBox<SpawnPoint> spawnPointPicker;
    private JSpinner ballCountSpinner;

    public static final int WIDTH = 450;
    public static final int HEIGHT = 350;
    public BounceFrame() {
        this.setSize(WIDTH, HEIGHT);
        this.setTitle("Bounce program");
        this.canvas = new BallCanvas();
        ballCountLabel = new JLabel("Balls in pockets: 0");
        ballCountLabel.setForeground(Color.BLACK);
        ballCountLabel.setFont(new Font("Arial", Font.BOLD, 16));

        System.out.println("In Frame Thread name = "
                + Thread.currentThread().getName());
        Container content = this.getContentPane();
        content.add(this.canvas, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(Color.lightGray);
        Thread ballCountThread = new Thread(() -> {
           while(true){
               UpdateCounter();
           }
        });
        ballCountThread.start();
        JButton buttonStart = new JButton("Start");
        JButton buttonStop = new JButton("Stop");

        colorPicker = new JComboBox<>(BallColor.values());
        spawnPointPicker = new JComboBox<>(SpawnPoint.values());
        SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 1000, 1);
        ballCountSpinner = new JSpinner(model);
        ((JSpinner.DefaultEditor) ballCountSpinner.getEditor()).getTextField().setColumns(3);

        buttonStart.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                BallColor selectedColor = (BallColor) colorPicker.getSelectedItem();
                SpawnPoint selectedSpawnPoint = (SpawnPoint) spawnPointPicker.getSelectedItem();
                int numBalls = (int) ballCountSpinner.getValue();

                for (int i = 0; i < numBalls; i++) {
                    Ball b = new Ball(canvas, selectedColor, selectedSpawnPoint);
                    canvas.add(b);
                    Thread thread = new Thread(new BallRunner(b));
                    switch (b.getColor()){
                        case RED:
                            thread.setPriority(Thread.MAX_PRIORITY);
                            break;
                        case YELLOW:
                        case BLUE:
                            thread.setPriority(Thread.MIN_PRIORITY);
                            break;
                    }
                    thread.start();
                    System.out.println("Thread name = " +
                            thread.getName());
                }
            }
        });
        buttonStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                System.exit(0);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(buttonStart, gbc);
        gbc.gridx = 1;
        buttonPanel.add(buttonStop, gbc);
        gbc.gridx = 2;
        buttonPanel.add(new JLabel("Color:"), gbc);
        gbc.gridx = 3;
        buttonPanel.add(colorPicker, gbc);
        gbc.gridx = 4;
        buttonPanel.add(spawnPointPicker, gbc);
        gbc.gridx = 5;
        buttonPanel.add(new JLabel("Balls:"), gbc);
        gbc.gridx = 6;
        buttonPanel.add(ballCountSpinner, gbc);
        gbc.gridx = 7;
        buttonPanel.add(ballCountLabel, gbc);

        content.add(buttonPanel, BorderLayout.SOUTH);
    }

    public synchronized void UpdateCounter(){
        ballCountLabel.setText(String.format("%d",  canvas.getBallsInPocket()));
    }
}

