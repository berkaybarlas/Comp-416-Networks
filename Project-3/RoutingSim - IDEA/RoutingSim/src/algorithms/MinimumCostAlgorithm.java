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

        // Find the list of neighbors, excluding the previous hop.
        List<NeighborInfo> chosen = Arrays.asList(neighbors.stream()
                // Make sure that we do not route back to the previous hop.
                .filter(n -> !n.address.equals(previousHop)).min((i,j)-> Integer.compare(i.cost, j.cost))
                .orElseThrow(NoSuchElementException::new)) ;

        visited.add(previousHop);

        // Return the chosen nodes.
        return chosen;
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
