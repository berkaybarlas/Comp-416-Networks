package simulator;

import java.util.*;

public class Simulator {

    public static int MAX_ITERATIONS = 100;

    private HashMap<String, Node> nodes = new HashMap<>();
    private HashMap<Integer, Packet> inTransmit = new HashMap<>();
    private HashMap<Integer, Integer> inTransmitRounds = new HashMap<>();

    private Packet finalPacket = null;
    private int totalCost = 0;
    private int t = 0;

    public Simulator(List<Node> nodes) {
        for(Node n : nodes) {
            this.nodes.put(n.address, n);
        }
    }

    public void update() {
        //System.out.println("Simulator: t=" + t);
        Set<Integer> toReceive = new HashSet<>();
        // Find the messages that should be sent.
        for(Map.Entry<Integer, Integer> e : inTransmitRounds.entrySet()) {
            if(e.getValue() == 0) toReceive.add(e.getKey());
        }
        for(Integer msgId : toReceive) {
            Packet pkt = inTransmit.get(msgId);
            String targetAddr = pkt.nextHop;
            if(!nodes.containsKey(targetAddr)) {
                System.err.println("Simulator: Invalid address " + targetAddr);
            } else {
                // Run the forwarding algorithm of the node.
                nodes.get(targetAddr).receive(pkt, this);
            }
            inTransmitRounds.remove(msgId);
            inTransmit.remove(msgId);
        }
        // Move the messages.
        inTransmitRounds.replaceAll((i, v) -> inTransmitRounds.get(i) - 1);
        // Update the packet costs.
        inTransmit.values().forEach(Packet::updateCost);
        // Update the total cost.
        totalCost += inTransmitRounds.size();
        // Update the time.
        t++;
    }

    public void sendPacket(Node node, Packet pkt, NeighborInfo neighborInfo) {
        if(!nodes.containsKey(neighborInfo.address)) {
            System.err.println("Simulator: Invalid address " + neighborInfo.address);
            return;
        }
        if(!node.hasNeighbor(neighborInfo.address)) {
            System.err.println("Simulator: " + node.address + " does not have a neighbor with address " + neighborInfo.address);
        }
        // Update the packet.
        pkt.nextHop = neighborInfo.address;
        pkt.updatePath(neighborInfo.address);
        inTransmit.put(pkt.pktId, pkt);
        inTransmitRounds.put(pkt.pktId, neighborInfo.cost);
    }

    public void destinationReached(Packet pkt) {
        if(finalPacket == null) finalPacket = pkt.copy();
    }

    public void run(Packet pkt) {
        // Set the origin of the packet as the first hop.
        pkt.updatePath(pkt.origin);
        // Let the origin forward the packet.
        nodes.get(pkt.origin).receive(pkt, this);
        boolean halts = true;
        boolean destReached = true;
        while(true) {
            // If there are no messages that are in transmit, this means that the system has halted.
            if(inTransmit.isEmpty()) {
                break;
            }
            // We only iterate over MAX_ITERATIONS many time slots. Realistically, the algorithms should
            // terminate much earlier.
            if(t == MAX_ITERATIONS) {
                halts = false;
                break;
            }
            update();
        }
        // Check whether the destination was reached.
        if(finalPacket == null) {
            destReached = false;
        }

        System.out.println("Protocol converged? " + ((halts) ? "Yes!" : "No.") +
                "\nDestination reached? " + ((destReached) ? "Yes!" : "No.") +
                "\nSuccess? " + ((halts && destReached) ? "Yes!" : "No."));
        if(halts && destReached) {
            String pathRepr = String.join(" -> ", finalPacket.getPath());
            int pathLength = finalPacket.getPath().size();
            System.out.println("Results:");
            System.out.println("\tPath = " + pathRepr);
            System.out.println("\tPath length = " + pathLength);
            System.out.println("\tPath cost = " + finalPacket.getCost());
            System.out.println("\tTotal communication cost = " + totalCost);
        }
        inTransmit.clear();
        inTransmitRounds.clear();
        totalCost = 0;
        finalPacket = null;
        t = 0;
    }

}
