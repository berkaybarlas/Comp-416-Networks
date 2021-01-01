package algorithms;

import simulator.NeighborInfo;

import java.util.List;
import java.util.stream.Collectors;

public class NaiveFloodingAlgorithm extends Algorithm {

    @Override
    public List<NeighborInfo> selectNeighbors(String origin, String destination, String previousHop,
                                              List<NeighborInfo> neighbors) {
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
        return new NaiveFloodingAlgorithm();
    }

    @Override
    public String getName() {
        return "NaiveFlooding";
    }
}
