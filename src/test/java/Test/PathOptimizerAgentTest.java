package Test;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathOptimizerAgentTest {

    @Test
    void shortestPathShouldReturnNullWhenUnreachable() {
        PathOptimizerAgent agent = new PathOptimizerAgent();

        agent.addDirectedEdge("A", "B", 1);
        agent.addDirectedEdge("C", "D", 1);

        PathOptimizerAgent.PathResult result = agent.shortestPath("A", "D");

        assertNull(result);
        assertFalse(agent.isReachable("A", "D"));
    }

    @Test
    void addDirectedEdgeShouldRejectZeroWeight() {
        PathOptimizerAgent agent = new PathOptimizerAgent();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agent.addDirectedEdge("A", "B", 0)
        );

        assertTrue(exception.getMessage().contains("权重必须是正整数"));
    }

    @Test
    void addDirectedEdgeShouldRejectNegativeWeight() {
        PathOptimizerAgent agent = new PathOptimizerAgent();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agent.addDirectedEdge("A", "B", -1)
        );

        assertTrue(exception.getMessage().contains("权重必须是正整数"));
    }

    @Test
    void shouldReturnAllEqualShortestPaths() {
        PathOptimizerAgent agent = new PathOptimizerAgent();

        agent.addDirectedEdge("A", "B", 1);
        agent.addDirectedEdge("B", "D", 1);
        agent.addDirectedEdge("A", "C", 1);
        agent.addDirectedEdge("C", "D", 1);

        List<PathOptimizerAgent.PathResult> results = agent.getAllShortestPaths("A", "D");

        assertEquals(2, results.size());
        assertEquals(2, results.get(0).totalCost);

        assertTrue(results.stream()
                .anyMatch(r -> r.path.equals(java.util.Arrays.asList("A", "B", "D"))));

        assertTrue(results.stream()
                .anyMatch(r -> r.path.equals(java.util.Arrays.asList("A", "C", "D"))));
    }

    @Test
    void updateWeightShouldFailWhenEdgeDoesNotExist() {
        PathOptimizerAgent agent = new PathOptimizerAgent();

        agent.addDirectedEdge("A", "B", 1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agent.updateWeight("A", "C", 2)
        );

        assertTrue(exception.getMessage().contains("节点不存在")
                || exception.getMessage().contains("边不存在"));
    }

    @Test
    void removeEdgeShouldChangeShortestPath() {
        PathOptimizerAgent agent = new PathOptimizerAgent();

        agent.addDirectedEdge("A", "B", 1);
        agent.addDirectedEdge("B", "D", 1);
        agent.addDirectedEdge("A", "C", 1);
        agent.addDirectedEdge("C", "D", 5);

        PathOptimizerAgent.PathResult before = agent.shortestPath("A", "D");

        assertNotNull(before);
        assertEquals(java.util.Arrays.asList("A", "B", "D"), before.path);
        assertEquals(2, before.totalCost);

        agent.removeEdge("B", "D");

        PathOptimizerAgent.PathResult after = agent.shortestPath("A", "D");

        assertNotNull(after);
        assertEquals(java.util.Arrays.asList("A", "C", "D"), after.path);
        assertEquals(6, after.totalCost);
    }

    @Test
    void summarizeTopologyShouldCountNodesAndEdges() {
        PathOptimizerAgent agent = new PathOptimizerAgent();

        agent.addDirectedEdge("A", "B", 1);
        agent.addDirectedEdge("B", "D", 6);

        TopologySummary summary = agent.summarizeTopology();

        assertEquals(3, summary.getNodeCount());
        assertEquals(2, summary.getEdgeCount());
        assertTrue(summary.getIsolatedNodes().isEmpty());
        assertEquals(java.util.Collections.singletonList("D"), summary.getNoOutgoingNodes());
    }
}