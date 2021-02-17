package algorithms;

import simulator.NeighborInfo;

import java.util.*;
import java.util.stream.Collectors;

public class NaiveMinimumCostAlgorithm extends Algorithm {

    @Override
    public List<NeighborInfo> selectNeighbors(String origin, String destination, String previousHop,
                                              List<NeighborInfo> neighbors) {
        return new ArrayList<NeighborInfo>() {
            {
                add(neighbors.stream()
                        .filter(n -> !n.address.equals(previousHop))
                        .sorted(Comparator.comparingInt(o -> o.cost))
                        .collect(Collectors.toList()).get(0));
            }
        };
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
