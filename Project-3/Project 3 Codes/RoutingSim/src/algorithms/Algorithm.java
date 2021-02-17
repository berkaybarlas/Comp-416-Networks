package algorithms;

import simulator.NeighborInfo;

import java.util.List;

public abstract class Algorithm {
    public abstract List<NeighborInfo> selectNeighbors(String origin, String destination, String previousHop,
                                                       List<NeighborInfo> neighbors);
    public abstract Algorithm copy();
    public abstract String getName();
}
