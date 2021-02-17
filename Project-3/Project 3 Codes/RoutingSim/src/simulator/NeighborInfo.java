package simulator;

public class NeighborInfo {

    public final int cost;
    public final String address;

    public NeighborInfo(int cost, String address) {
        this.cost = cost;
        this.address = address;
    }

    public NeighborInfo copy() {
        return new NeighborInfo(cost, address);
    }
}
