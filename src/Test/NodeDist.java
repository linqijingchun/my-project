package Test;

class NodeDist implements Comparable<NodeDist> {
    String node;
    int dist;
    NodeDist(String node, int dist) {
        this.node = node;
        this.dist = dist;
    }
    @Override
    public int compareTo(NodeDist other) {
        return Integer.compare(this.dist, other.dist);
    }
}