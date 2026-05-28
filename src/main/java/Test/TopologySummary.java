package Test;

import java.util.Collections;
import java.util.List;

class TopologySummary {
    private final int nodeCount;
    private final int edgeCount;
    private final List<String> isolatedNodes;
    private final List<String> noOutgoingNodes;

    TopologySummary(int nodeCount,
                    int edgeCount,
                    List<String> isolatedNodes,
                    List<String> noOutgoingNodes) {
        this.nodeCount = nodeCount;
        this.edgeCount = edgeCount;
        this.isolatedNodes = Collections.unmodifiableList(isolatedNodes);
        this.noOutgoingNodes = Collections.unmodifiableList(noOutgoingNodes);
    }

    int getNodeCount() {
        return nodeCount;
    }

    int getEdgeCount() {
        return edgeCount;
    }

    List<String> getIsolatedNodes() {
        return isolatedNodes;
    }

    List<String> getNoOutgoingNodes() {
        return noOutgoingNodes;
    }
}