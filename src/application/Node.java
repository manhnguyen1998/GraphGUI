package application;

import java.util.ArrayList;
import java.util.List;

public class Node implements Comparable<Node> {
    public String name;
    public List<Edge> adjacents = new ArrayList<Edge>();
    public Node previous;
    public SceneController.NodeFX circle;
    public double minDistance = Double.POSITIVE_INFINITY;
    public boolean visited;
    public int DAGColor;

    public Node(String argName) {
        name = argName;
        visited = false;
    }

    public Node(String argName, SceneController.NodeFX c) {
        name = argName;
        circle = c;
        visited = false;
    }

    @Override
    public int compareTo(Node o) {
        return Double.compare(minDistance, o.minDistance);
    }
}
