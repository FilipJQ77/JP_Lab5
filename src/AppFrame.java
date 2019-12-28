/*
Laboratoria 5: Aplikacja wielowątkowa o autobusach na moście
Autor: Filip Przygoński, 248892
Data: Grudzień 2019
*/

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class AnimationPanel extends JPanel {

    public static int parking1 = 0;
    public static int road1 = 90;
    public static int gate1 = 180;
    public static int bridge = 270;
    public static int gate2 = 360;
    public static int road2 = 450;
    public static int parking2 = 540;

    ArrayList<Bus> busesDriving = new ArrayList<>();

    public AnimationPanel() {
        setSize(630, 720);

        setVisible(true);
        Runnable refreshPanelThread = () -> {
            while (true) {
                this.repaint();
                try {
                    Thread.sleep(15);
                } catch (InterruptedException ignored) {}
            }
        };
        new Thread(refreshPanelThread).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = 90;
        int height = this.getHeight();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(parking1, 0, width, height);
        g.fillRect(parking2, 0, width, height);
        g.setColor(Color.GRAY);
        g.fillRect(road1, 0, width, height);
        g.fillRect(road2, 0, width, height);
        g.fillRect(bridge, 0, width, height);
        g.setColor(Color.RED);
        g.fillRect(gate1, 0, width, height);
        g.fillRect(gate2, 0, width, height);
        g.setColor(Color.BLACK);
        g.drawString("PARKING", parking1, 10);
        g.drawString("DROGA", road1, 10);
        g.drawString("BRAMKA", gate1, 10);
        g.drawString("MOST", bridge, 10);
        g.drawString("BRAMKA", gate2, 10);
        g.drawString("DROGA", road2, 10);
        g.drawString("PARKING", parking2, 10);
        int y = 0;
        synchronized (busesDriving) {
            for (Bus bus : busesDriving) {
                bus.paint(g, y);
                y += 50;
            }
        }
    }
}

class AnimationFrame extends JFrame {

    AnimationPanel animationPanel;

    public AnimationFrame(AppFrame frame) {
        super("Animacja autobusów");
        setSize(640, 720);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(frame);
        setLocation(frame.getWidth() + 10, 0);
        animationPanel = new AnimationPanel();
        setContentPane(animationPanel);
        setVisible(true);
    }
}

public class AppFrame extends JFrame {

    public static final int MIN_TRAFFIC = 500;
    public static final int MAX_TRAFFIC = 4500;

    private static int trafficNumber = 1000;

    Bridge bridge;

    JPanel contentPanel = new JPanel();
    AnimationFrame animationFrame;

    JLabel bridgeTypeLabel = new JLabel("Typ mostu");
    JComboBox<BridgeType> bridgeTypeChooser = new JComboBox<>(BridgeType.values());
    JLabel trafficLabel = new JLabel("Natężenie ruchu");
    JSlider trafficSlider = new JSlider(MIN_TRAFFIC, MAX_TRAFFIC, 1000);
    JLabel busesOnBridgeLabel = new JLabel("Autobusy na moście");
    JTextField busesOnBridgeField = new JTextField(50){
        @Override
        public boolean isEditable() {
            return false;
        }
    };
    JLabel busesWaitingLabel = new JLabel("Autobusy czekające");
    JTextField busesWaitingField = new JTextField(50){
        @Override
        public boolean isEditable() {
            return false;
        }
    };
    JTextArea console = new JTextArea(25, 10){
        @Override
        public boolean isEditable() {
            return false;
        }
    };
    JScrollPane consolePane = new JScrollPane(console);

    public AppFrame() {
        super("Autobusy na moście");
        setSize(640, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JMenuItem menuAuthor = new JMenuItem("Autor");
        menuAuthor.addActionListener(action -> JOptionPane.showMessageDialog(this, "Autor: Filip Przygoński, 248892, Grudzień 2019"));
        JMenuItem menuExit = new JMenuItem("Zakończ");
        menuExit.addActionListener(action -> System.exit(0));
        JMenu menu = new JMenu("Menu");
        menu.add(menuAuthor);
        menu.add(menuExit);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        setJMenuBar(menuBar);

        contentPanel.add(bridgeTypeLabel);
        contentPanel.add(bridgeTypeChooser);
        bridgeTypeChooser.addActionListener(a -> bridge.setType((BridgeType) bridgeTypeChooser.getSelectedItem()));
        contentPanel.add(trafficLabel);
        contentPanel.add(trafficSlider);
        trafficSlider.addChangeListener(e -> trafficNumber = trafficSlider.getValue());
        contentPanel.add(busesOnBridgeLabel);
        contentPanel.add(busesOnBridgeField);
        contentPanel.add(busesWaitingLabel);
        contentPanel.add(busesWaitingField);

        consolePane.setPreferredSize(new Dimension(getWidth() - 30, getHeight() - 200));
        consolePane.getVerticalScrollBar().setUnitIncrement(20);
        consolePane.getHorizontalScrollBar().setUnitIncrement(20);
        consolePane.setVisible(true);
        contentPanel.add(consolePane);

        setContentPane(contentPanel);
        setVisible(true);
        animationFrame = new AnimationFrame(this);
    }

    public static void main(String[] args) {
        AppFrame frame = new AppFrame();
        frame.bridge = new Bridge(frame);
        while (true) {
            try {
                Thread.sleep(5000 - trafficNumber);
            } catch (InterruptedException ignored) {
            }
            Bus jebus = new Bus(frame.bridge, frame);
            new Thread(jebus).start();
        }
    }
}
