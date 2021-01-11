package algorithms;

import simulator.NeighborInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements a flooding routing algorithm that converges.
 */
public class FloodingAlgorithm extends Algorithm {

    // Visited flag
    boolean visited = false;

    @Override
    public List<NeighborInfo> selectNeighbors(String origin, String destination, String previousHop,
                                              List<NeighborInfo> neighbors) {

        System.out.println(origin + "->" + destination + " from " + previousHop + " " + neighbors.toString());
        // Check visited flag
        if (visited) {
            // Return empty list after first call
            return new ArrayList();
        }
        // Set visited flag true
        visited = true;

        // Find the list of neighbors, excluding the previous hop.
        List<NeighborInfo> chosen = neighbors.stream()
                // Make sure that we do not route back to the previous hop.
                .filter(n -> !n.address.equals(previousHop))
                .collect(Collectors.toList());

        // Return the chosen nodes.
        return chosen;
    }

    @Override
    public Algorithm copy() {
        return new FloodingAlgorithm();
    }

    @Override
    public String getName() {
        return "Flooding";
    }
}
