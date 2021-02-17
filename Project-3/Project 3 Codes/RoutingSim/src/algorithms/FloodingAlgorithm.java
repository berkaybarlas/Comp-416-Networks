package algorithms;

import simulator.NeighborInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements a flooding routing algorithm that converges.
 */
public class FloodingAlgorithm extends Algorithm {

    // IMPORTANT: You can maintain a state, e.g., a flag.
    int flag = 1;

    @Override
    public List<NeighborInfo> selectNeighbors(String origin, String destination, String previousHop,
                                              List<NeighborInfo> neighbors) {

        if(flag == 1)
        {
            flag = 0;
            return neighbors.stream()
                    .filter(n -> !n.address.equals(previousHop))
                    .collect(Collectors.toList());
        }
        return null;
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
