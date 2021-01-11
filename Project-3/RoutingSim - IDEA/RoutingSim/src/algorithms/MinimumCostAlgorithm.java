package algorithms;

import simulator.NeighborInfo;

import java.util.*;
import java.util.stream.Collectors;

public class MinimumCostAlgorithm extends Algorithm {

    // IMPORTANT: Use this random number generator.
    Random rand = new Random(6391238);

    // IMPORTANT: You can maintain a state, e.g., a set of neighbors.
    HashSet<String> visited = new HashSet<>();

    @Override
    public List<NeighborInfo> selectNeighbors(String origin, String destination, String previousHop,
                                              List<NeighborInfo> neighbors) {

        if (visited.size() >= neighbors.size()) {
            int selectedIndex = rand.nextInt(neighbors.size());
            // Return the randomly chosen node.
            return Arrays.asList(neighbors.get(selectedIndex));
        }

        visited.add(previousHop);
        // Find the list of neighbors, excluding the previous hop.
        NeighborInfo chosen = neighbors.stream()
                // Make sure that we do not route back to the previous hop.
                .filter(n -> !visited.contains(n.address))
                .min(Comparator.comparingInt(i -> i.cost))
                .orElseThrow(NoSuchElementException::new);

        visited.add(chosen.address);

        // Return the chosen nodes.
        return Arrays.asList(chosen);
    }

    @Override
    public Algorithm copy() {
        return new MinimumCostAlgorithm();
    }

    @Override
    public String getName() {
        return "MinimumCost";
    }
}
