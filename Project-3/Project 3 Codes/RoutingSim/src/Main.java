import algorithms.*;
import simulator.*;

import java.util.List;

public class Main {

    // Declare the algorithms to simulate.
    private static final Algorithm[] algorithms = {
            new NaiveFloodingAlgorithm(),
            new FloodingAlgorithm(),
            new NaiveMinimumCostAlgorithm(),
            new MinimumCostAlgorithm()
    };

    // Declare the topologies to be simulated over.
    private static final String[] topologyFilePaths = {
            "topologies/topology1.txt", "topologies/topology2.txt"
    };

    public static void main(String[] args) {
        for(Algorithm a : algorithms) {
            for (String t : topologyFilePaths) {
                System.out.println("Simulating " + a.getName() + " with topology " + t);
                simulateTopology(t, a);
                System.out.println();
            }
        }
    }

    /**
     * Simulates the given algorithm over the topology file residing at the given file path.
     * @param topologyFilePath the topology file path.
     * @param fixture the algorithm instance.
     */
    public static void simulateTopology(String topologyFilePath, Algorithm fixture) {
        // Read the topology.
        List<Node> topology = Topology.readTopology(topologyFilePath);
        if(topology == null || topology.isEmpty()) {
            return;
        }
        // Set the routing algorithm.
        topology.forEach(n -> n.setRoutingAlgorithm(fixture.copy()));
        // Create the simulator.
        Simulator simulator = new Simulator(topology);
        // Choose the origin and the destination.
        String origin = topology.get(0).address;
        String destination = topology.get(topology.size()-1).address;
        simulator.run(new Packet(origin, destination, -1));
    }
}
