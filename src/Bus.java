/*
Laboratoria 5: Aplikacja wielowątkowa o autobusach na moście
Autor: Filip Przygoński, 248892
Data: Grudzień 2019
*/

import java.awt.*;
import java.util.Random;

enum BusDirection {

    LEFT,
    RIGHT;

    @Override
    public String toString() {
        switch (this) {
            case LEFT:
                return "Lewo";
            case RIGHT:
                return "Prawo";
        }
        return "";
    }
}

enum BusState {

    BOARDING,
    DRIVING_TO_BRIDGE,
    WAITING_AT_GATE,
    DRIVING_THROUGH_BRIDGE,
    DRIVING_TO_PARKING,
    UNBOARDING;

}

public class Bus implements Runnable {

    public static final int MIN_BOARDING_TIME = 1000;
    public static final int MAX_BOARDING_TIME = 3000;
    public static final int DRIVING_BETWEEN_BRIDGE_AND_PARKING_TIME = 2500;
    public static final int DRIVING_THROUGH_BRIDGE_TIME = 2000;
    private static int busCounter = 0;

    private int boardingTime;
    private int busNumber;
    private BusDirection direction;
    private BusState state;
    private Bridge bridge;
    private AppFrame frame;

    private int currentXPosition;

    public Bus(Bridge bridge, AppFrame frame) {
        synchronized ((Integer) busCounter) {
            busNumber = ++busCounter;
        }
        Random rand = new Random();
        boardingTime = rand.nextInt(MAX_BOARDING_TIME - MIN_BOARDING_TIME) + MIN_BOARDING_TIME;
        if (rand.nextBoolean()) {
            direction = BusDirection.RIGHT;
        } else direction = BusDirection.LEFT;
        switch (direction) {
            case RIGHT:
                currentXPosition = AnimationPanel.parking1 + 10;
                break;
            case LEFT:
                currentXPosition = AnimationPanel.parking2 + 10;
                break;
        }
        state = BusState.BOARDING;
        this.bridge = bridge;
        this.frame = frame;
    }

    void showBusInfo(String message) {
        String info = "Autobus[" + busNumber + "->" + direction + "]: " + message + "\n";
        frame.console.insert(info, 0);
    }

    void boarding() {
        showBusInfo("Pasażerowie wsiadają");
        try {
            Thread.sleep(boardingTime);
        } catch (InterruptedException ignored) {
        }
    }

    void drivingToBridge() {
        synchronized ((Integer) currentXPosition) {
            switch (direction) {
                case RIGHT:
                    currentXPosition = AnimationPanel.road1 + 10;
                    break;
                case LEFT:
                    currentXPosition = AnimationPanel.road2 + 10;
                    break;
            }
        }
        synchronized (state) {
            state = BusState.DRIVING_TO_BRIDGE;
        }
        showBusInfo("Jazda w stronę mostu");
        try {
            Thread.sleep(DRIVING_BETWEEN_BRIDGE_AND_PARKING_TIME);
        } catch (InterruptedException ignored) {
        }
    }

    void tryToDriveOntoTheBridge(Bridge bridge) {
        synchronized ((Integer) currentXPosition) {
            switch (direction) {
                case RIGHT:
                    currentXPosition = AnimationPanel.gate1 + 10;
                    break;
                case LEFT:
                    currentXPosition = AnimationPanel.gate2 + 10;
                    break;
            }
        }
        synchronized (state) {
            state = BusState.WAITING_AT_GATE;
        }
        bridge.busTriesToDriveOntoTheBridge(this);
    }

    void drivingThroughBridge() {
        synchronized ((Integer) currentXPosition) {
            currentXPosition = AnimationPanel.bridge + 10;
        }
        synchronized (state) {
            state = BusState.DRIVING_THROUGH_BRIDGE;
        }
        showBusInfo("Przejazd przez most");
        try {
            Thread.sleep(DRIVING_THROUGH_BRIDGE_TIME);
        } catch (InterruptedException ignored) {
        }
    }

    void driveOffTheBridge(Bridge bridge) {
        synchronized ((Integer) currentXPosition) {
            switch (direction) {
                case RIGHT:
                    currentXPosition = AnimationPanel.gate2 + 10;
                    break;
                case LEFT:
                    currentXPosition = AnimationPanel.gate1 + 10;
                    break;
            }
        }
        bridge.busDrivesOffABridge(this);
    }

    void drivingToParking() {
        synchronized ((Integer) currentXPosition) {
            switch (direction) {
                case RIGHT:
                    currentXPosition = AnimationPanel.road2 + 10;
                    break;
                case LEFT:
                    currentXPosition = AnimationPanel.road1 + 10;
                    break;
            }
        }
        synchronized (state) {
            state = BusState.DRIVING_TO_PARKING;
        }
        showBusInfo("Jazda w stronę parkingu");
        try {
            Thread.sleep(DRIVING_BETWEEN_BRIDGE_AND_PARKING_TIME);
        } catch (InterruptedException ignored) {
        }
    }

    void unboarding() {
        synchronized ((Integer) currentXPosition) {
            switch (direction) {
                case RIGHT:
                    currentXPosition = AnimationPanel.parking2 + 10;
                    break;
                case LEFT:
                    currentXPosition = AnimationPanel.parking1 + 10;
                    break;
            }
        }
        synchronized (state) {
            state = BusState.UNBOARDING;
        }
        showBusInfo("Pasażerowie wysiadają");
        try {
            Thread.sleep(boardingTime);
        } catch (InterruptedException ignored) {
        }
    }

    public int getBusNumber() {
        return busNumber;
    }

    public BusDirection getDirection() {
        return direction;
    }

    public void paint(Graphics g, int y) {
        //podstawowa animacja
        synchronized ((Integer) currentXPosition) {
            g.setColor(Color.BLACK);
            g.fillOval(currentXPosition + 5, y + 55, 10, 10);
            g.fillOval(currentXPosition + 55, y + 55, 10, 10);
            g.setColor(Color.BLUE);
            g.fillRect(currentXPosition, 20 + y, 70, 40);
            g.setColor(Color.WHITE);
            g.drawString(String.valueOf(busNumber), currentXPosition + 1, 32 + y);
            if (direction.equals(BusDirection.RIGHT)) {
                g.fillRect(currentXPosition + 65, y + 50, 5, 5);
                g.setColor(Color.RED);
                g.fillRect(currentXPosition, y + 50, 5, 5);
            } else {
                g.fillRect(currentXPosition, y + 50, 5, 5);
                g.setColor(Color.RED);
                g.fillRect(currentXPosition + 65, y + 50, 5, 5);
            }
            g.setColor(Color.WHITE);
            synchronized (state) {
                switch (state) {
                    case BOARDING:
                        g.drawString("BOARDING", currentXPosition + 1, 45 + y);
                        break;
                    case DRIVING_TO_BRIDGE:
                    case DRIVING_THROUGH_BRIDGE:
                    case DRIVING_TO_PARKING:
                        g.drawString("DRIVING", currentXPosition + 1, 45 + y);
                        break;
                    case WAITING_AT_GATE:
                        g.drawString("WAITING", currentXPosition + 1, 45 + y);
                        break;
                    case UNBOARDING:
                        g.drawString("FINISHING", currentXPosition + 1, 45 + y);
                        break;
                }
            }
        }
    }

    @Override
    public void run() {
        synchronized (frame.animationFrame.animationPanel.busesDriving) {
            frame.animationFrame.animationPanel.busesDriving.add(this);
        }
        boarding();
        drivingToBridge();
        tryToDriveOntoTheBridge(bridge);
        drivingThroughBridge();
        driveOffTheBridge(bridge);
        drivingToParking();
        unboarding();
        showBusInfo("Koniec trasy");
        synchronized (frame.animationFrame.animationPanel.busesDriving) {
            frame.animationFrame.animationPanel.busesDriving.remove(this);
        }
    }
}
