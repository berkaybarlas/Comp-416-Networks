package algorithms;

import simulator.NeighborInfo;

import java.util.*;
import java.util.stream.Collectors;

public class MinimumCostAlgorithm extends Algorithm {

    // IMPORTANT: Use this random number generator.
    Random rand = new Random(6391238);

    // IMPORTANT: You can maintain a state, e.g., a set of neighbors.
    List<String> exclusionSet = new ArrayList<>();
    NeighborInfo chosen = null;
    @Override
    public List<NeighborInfo> selectNeighbors(String origin, String destination, String previousHop,
                                              List<NeighborInfo> neighbors) {

        // adds the previous node to the exclusion set if it isn't there already
        if(!exclusionSet.contains(previousHop) && previousHop != null)
        {
            exclusionSet.add(previousHop);
        }

        // finds neighbors that are not in the exclusion set
        List<NeighborInfo> nonExcludedNs = neighbors.stream().filter(n -> !exclusionSet.contains(n.address)).collect(Collectors.toList());

        // If all the neighbors are excluded, picks one at random
        if(nonExcludedNs.size() == 0)
        {
            chosen = neighbors.get(rand.nextInt(neighbors.size()));
        }

        // else, picks the one with the smallest cost
        else {
            nonExcludedNs.sort(Comparator.comparingInt(o -> o.cost));
            chosen = nonExcludedNs.get(0);
        }

        // adds the selection to the exclusion set if it isn't there already
        if(!exclusionSet.contains(chosen.address))
        {
            exclusionSet.add(chosen.address);
        }

        // prints the exclusion set
        //System.out.println(Arrays.toString(exclusionSet.toArray()));

        // returns an array with the chosen node in it
        return new ArrayList<>() {{
            add(chosen);
        }};
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
