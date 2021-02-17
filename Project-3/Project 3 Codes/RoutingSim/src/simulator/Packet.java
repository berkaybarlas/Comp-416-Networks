package simulator;

import java.util.LinkedList;
import java.util.List;

public class Packet {

    private static int lastId = 0;

    public String nextHop;

    public final String origin;
    public final String destination;
    public final int pktId;

    private List<String> pathSoFar = new LinkedList<>();
    private int costSoFar = 0;

    public Packet(String origin, String destination, int pktId) {
        this.origin = origin;
        this.destination = destination;
        this.pktId = (pktId >= 0) ? pktId : lastId++;
    }

    public void updatePath(String newHop) {
        pathSoFar.add(newHop);
    }
    public void updateCost() {
        costSoFar++;
    }

    public List<String> getPath() {
        return pathSoFar;
    }
    public int getCost() {
        return costSoFar;
    }

    public Packet copy() {
        Packet copy = new Packet(origin, destination, pktId);
        copy.nextHop = this.nextHop;
        copy.pathSoFar = new LinkedList<>(this.pathSoFar);
        copy.costSoFar = this.costSoFar;
        return copy;
    }

    public Packet copyWithNewId() {
        Packet copy = new Packet(origin, destination, lastId++);
        copy.nextHop = this.nextHop;
        copy.pathSoFar = new LinkedList<>(this.pathSoFar);
        copy.costSoFar = this.costSoFar;
        return copy;
    }
}
