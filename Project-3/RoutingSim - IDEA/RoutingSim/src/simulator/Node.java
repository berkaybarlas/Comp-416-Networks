package simulator;

import algorithms.Algorithm;

import java.util.List;
import java.util.stream.Collectors;

public class Node {

    public final String address;
    private Algorithm routingAlgorithm;
    private List<NeighborInfo> neighbors = null;

    public Node(String address, Algorithm routingAlgorithm) {
        this.address = address;
        this.routingAlgorithm = routingAlgorithm;
    }

    public void setRoutingAlgorithm(Algorithm routingAlgorithm) {
        this.routingAlgorithm = routingAlgorithm;
    }

    public void setNeighbors(List<NeighborInfo> neighbors) {
        this.neighbors = neighbors;
    }

    public boolean hasNeighbor(String address) {
        if(neighbors == null) return false;
        return neighbors.stream().anyMatch(x -> x.address.equals(address));
    }

    public void receive(Packet pkt, Simulator simulator) {
        if(pkt.destination.equals(address)) {
            simulator.destinationReached(pkt.copy());
            return;
        }
        List<NeighborInfo> copiedNeighbors = neighbors.stream()
                .map(NeighborInfo::copy)
                .collect(Collectors.toList());
        // Find the previous hop from the path information saved in the packet.
        String previousHop = (pkt.getPath().size() <= 1) ? null : pkt.getPath().get(pkt.getPath().size()-2);
        List<NeighborInfo> nextHops = routingAlgorithm.selectNeighbors(pkt.origin, pkt.destination, previousHop,
                copiedNeighbors);
        if(nextHops == null) return;
        for(NeighborInfo nextHop : nextHops) {
            Packet newPkt = pkt.copyWithNewId();
            simulator.sendPacket(this, newPkt, nextHop.copy());
        }
    }

}
