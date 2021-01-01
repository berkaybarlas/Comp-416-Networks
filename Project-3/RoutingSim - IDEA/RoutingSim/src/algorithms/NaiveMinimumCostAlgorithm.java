package algorithms;

import simulator.NeighborInfo;

import java.util.*;
import java.util.stream.Collectors;

public class NaiveMinimumCostAlgorithm extends Algorithm {

    @Override
    public List<NeighborInfo> selectNeighbors(String origin, String destination, String previousHop,
                                              List<NeighborInfo> neighbors) {
        // Find the list of neighbors, excluding the previous hop.
        List<NeighborInfo> chosen = Arrays.asList(neighbors.stream()
                // Make sure that we do not route back to the previous hop.
                .filter(n -> !n.address.equals(previousHop)).min((i,j)-> Integer.compare(i.cost, j.cost))
                .orElseThrow(NoSuchElementException::new)) ;
        // Return the chosen nodes.
        return chosen;
    }

    @Override
    public Algorithm copy() {
        return new NaiveMinimumCostAlgorithm();
    }

    @Override
    public String getName() {
        return "NaiveMinimumCost";
    }
}
