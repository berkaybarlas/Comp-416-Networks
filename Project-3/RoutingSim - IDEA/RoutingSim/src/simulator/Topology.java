package simulator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Topology {

    public static List<Node> readTopology(String filePath) {
        Map<String, Node> nodes = new HashMap<>();
        Map<String, List<NeighborInfo>> neighbors = new HashMap<>();
        BufferedReader bufrd = null;
        String nodesDeclaration = null;
        try {
            bufrd = new BufferedReader(new FileReader(filePath));
            nodesDeclaration = bufrd.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.print("Topology: Could not read file: " + filePath);
            return null;
        }
        String[] nodeAddresses = nodesDeclaration.split(",");
        // Create the nodes and their neighbor lists.
        for(String addr : nodeAddresses) {
            nodes.put(addr, new Node(addr, null));
            neighbors.put(addr, new LinkedList<>());
        }
        // Populate the neighbor lists.
        bufrd.lines().forEach(linkDeclaration -> {
            String[] declTokens = linkDeclaration.split(" ");
            String n1 = declTokens[0];
            String n2 = declTokens[1];
            int cost = Integer.parseInt(declTokens[2]);
            neighbors.get(n1).add(new NeighborInfo(cost, n2));
            neighbors.get(n2).add(new NeighborInfo(cost, n1));
        });
        try {
            bufrd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Save the neighbor lists.
        neighbors.forEach((nodeAddr, neighborList) -> {
            nodes.get(nodeAddr).setNeighbors(neighborList);
        });
        return new ArrayList<>(nodes.values());
    }
}
