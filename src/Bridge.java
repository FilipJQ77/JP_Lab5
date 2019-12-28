/*
Laboratoria 5: Aplikacja wielowątkowa o autobusach na moście
Autor: Filip Przygoński, 248892
Data: Grudzień 2019
*/

import java.util.ArrayList;

enum BridgeType {
    ONE_BUS,
    THREE_BUSES_ONE_DIRECTION,
    THREE_BUSES_BOTH_DIRECTIONS,
    UNLIMITED_BUSES;

    @Override
    public String toString() {
        switch (this) {
            case ONE_BUS:
                return "Tylko jeden autobus";
            case THREE_BUSES_ONE_DIRECTION:
                return "Trzy autobusy, w tym samym kierunku";
            case THREE_BUSES_BOTH_DIRECTIONS:
                return "Trzy autobusy, w obu kierunkach";
            case UNLIMITED_BUSES:
                return "Bez ograniczeń";
            default:
                return "";
        }
    }
}

public class Bridge {

    private ArrayList<Bus> busesWaiting;
    private ArrayList<Bus> busesOnBridge;
    private boolean canGoOnBridge;
    private BridgeType type;
    private BusDirection direction = BusDirection.RIGHT;
    private byte howManyBusesWentInOneDirection = 0;
    private AppFrame frame;

    public Bridge(AppFrame frame) {
        busesWaiting = new ArrayList<>() {
            @Override
            public String toString() {
                StringBuilder stringBuilder = new StringBuilder();
                for (Bus bus : this) {
                    stringBuilder.append(bus.getBusNumber()).append("->").append(bus.getDirection()).append(" ");
                }
                return stringBuilder.toString();
            }
        };
        busesOnBridge = new ArrayList<>() {
            @Override
            public String toString() {
                StringBuilder stringBuilder = new StringBuilder();
                for (Bus bus : this) {
                    stringBuilder.append(bus.getBusNumber()).append("->").append(bus.getDirection()).append(" ");
                }
                return stringBuilder.toString();
            }
        };
        canGoOnBridge = true;
        type = BridgeType.ONE_BUS;
        this.frame = frame;
    }

    private void checkIfItsPossibleToDriveOntoTheBridge(Bus bus) {
        switch (type) {
            case ONE_BUS:
                canGoOnBridge = busesOnBridge.isEmpty();
                break;
            case THREE_BUSES_ONE_DIRECTION:
                int howManyBusesOnBridge = busesOnBridge.size();
                if (howManyBusesOnBridge == 0) {
                    if (howManyBusesWentInOneDirection > 11) {
                        if (bus.getDirection().equals(direction)) {
                            canGoOnBridge = true;
                            howManyBusesWentInOneDirection = 1;
                        } else canGoOnBridge = false;
                    } else {
                        canGoOnBridge = true;
                        direction = bus.getDirection();
                        howManyBusesWentInOneDirection = 1;
                    }
                } else {
                    if (howManyBusesWentInOneDirection > 11) {
                        direction = direction.equals(BusDirection.LEFT) ? BusDirection.RIGHT : BusDirection.LEFT;
                        canGoOnBridge = false;
                    } else if (bus.getDirection().equals(direction) && howManyBusesOnBridge < 3) {
                        canGoOnBridge = true;
                        ++howManyBusesWentInOneDirection;
                    } else canGoOnBridge = false;
                }
                break;
            case THREE_BUSES_BOTH_DIRECTIONS:
                canGoOnBridge = busesOnBridge.size() < 3;
                break;
            case UNLIMITED_BUSES:
                canGoOnBridge = true;
                break;
        }
    }

    synchronized void busTriesToDriveOntoTheBridge(Bus bus) {
        busesWaiting.add(bus);
        frame.busesWaitingField.setText(busesWaiting.toString());
        checkIfItsPossibleToDriveOntoTheBridge(bus);
        while (!canGoOnBridge) {
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
            checkIfItsPossibleToDriveOntoTheBridge(bus);
        }
        busesWaiting.remove(bus);
        frame.busesWaitingField.setText(busesWaiting.toString());
        busesOnBridge.add(bus);
        frame.busesOnBridgeField.setText(busesOnBridge.toString());
    }

    synchronized void busDrivesOffABridge(Bus bus) {
        busesOnBridge.remove(bus);
        frame.busesOnBridgeField.setText(busesOnBridge.toString());
        notifyAll();
    }

    public void setType(BridgeType type) {
        this.type = type;
    }
}
