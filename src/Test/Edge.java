package Test;

class Edge {
    String target;
    int weight;
    Edge(String target, int weight) {
        this.target = target;
        this.weight = weight;
    }
    @Override
    public String toString() {
        return target + "(" + weight + ")";
    }
}