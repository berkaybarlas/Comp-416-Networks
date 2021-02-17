package algorithms;

import simulator.NeighborInfo;

import java.util.List;
import java.util.stream.Collectors;

public class NaiveFloodingAlgorithm extends Algorithm {

    @Override
    public List<NeighborInfo> selectNeighbors(String origin, String destination, String previousHop,
                                              List<NeighborInfo> neighbors) {
        List<NeighborInfo> chosen = neighbors.stream()
                .filter(n -> !n.address.equals(previousHop))
                .collect(Collectors.toList());
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
