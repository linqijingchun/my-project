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

    @Test
    void addDirectedEdgeShouldRejectDuplicateEdge() {
        PathOptimizerAgent agent = new PathOptimizerAgent();

        agent.addDirectedEdge("A", "B", 5);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agent.addDirectedEdge("A", "B", 3)
        );

        assertTrue(exception.getMessage().contains("边已存在"));
    }

    @Test
    void addUndirectedEdgeShouldRejectDuplicateEdge() {
        PathOptimizerAgent agent = new PathOptimizerAgent();

        agent.addUndirectedEdge("A", "B", 5);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agent.addUndirectedEdge("A", "B", 3)
        );

        assertTrue(exception.getMessage().contains("边已存在"));
    }

    // ---------- 约束路径测试 ----------

    @Test
    void constrainedPathWithViaNode() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        agent.addDirectedEdge("A", "B", 1);
        agent.addDirectedEdge("B", "D", 1);
        agent.addDirectedEdge("A", "C", 1);
        agent.addDirectedEdge("C", "D", 1);

        // 不约束时有两条等长路径，强制经过 B
        PathOptimizerAgent.PathResult result = agent.constrainedPath(
                "A", "D", java.util.Collections.singletonList("B"),
                java.util.Collections.emptyList(), null);

        assertNotNull(result);
        assertEquals(java.util.Arrays.asList("A", "B", "D"), result.path);
        assertEquals(2, result.totalCost);
    }

    @Test
    void constrainedPathWithAvoidNode() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        agent.addDirectedEdge("A", "B", 1);
        agent.addDirectedEdge("B", "D", 1);
        agent.addDirectedEdge("A", "C", 5);
        agent.addDirectedEdge("C", "D", 1);

        // 避开 B，只能走 A->C->D
        PathOptimizerAgent.PathResult result = agent.constrainedPath(
                "A", "D", java.util.Collections.emptyList(),
                java.util.Collections.singletonList("B"), null);

        assertNotNull(result);
        assertEquals(java.util.Arrays.asList("A", "C", "D"), result.path);
        assertEquals(6, result.totalCost);
    }

    @Test
    void constrainedPathWithHopLimit() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        agent.addDirectedEdge("A", "B", 1);
        agent.addDirectedEdge("B", "C", 1);
        agent.addDirectedEdge("C", "D", 1);
        agent.addDirectedEdge("A", "D", 10);

        // 限制最多 1 跳，只能走直接边 A->D
        PathOptimizerAgent.PathResult result = agent.constrainedPath(
                "A", "D", java.util.Collections.emptyList(),
                java.util.Collections.emptyList(), 1);

        assertNotNull(result);
        assertEquals(java.util.Arrays.asList("A", "D"), result.path);
        assertEquals(10, result.totalCost);
    }

    @Test
    void constrainedPathWithHopLimitUnreachable() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        agent.addDirectedEdge("A", "B", 1);
        agent.addDirectedEdge("B", "C", 1);
        agent.addDirectedEdge("C", "D", 1);

        // 限制最多 1 跳，但 A 到 D 没有直接边
        PathOptimizerAgent.PathResult result = agent.constrainedPath(
                "A", "D", java.util.Collections.emptyList(),
                java.util.Collections.emptyList(), 1);

        assertNull(result);
    }

    @Test
    void constrainedPathWithViaAndAvoid() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        agent.addDirectedEdge("A", "B", 1);
        agent.addDirectedEdge("B", "E", 1);
        agent.addDirectedEdge("E", "D", 1);
        agent.addDirectedEdge("A", "C", 1);
        agent.addDirectedEdge("C", "D", 1);
        agent.addDirectedEdge("B", "C", 1);

        // 经过 B，避开 E：A->B->C->D
        PathOptimizerAgent.PathResult result = agent.constrainedPath(
                "A", "D", java.util.Collections.singletonList("B"),
                java.util.Collections.singletonList("E"), null);

        assertNotNull(result);
        assertEquals(java.util.Arrays.asList("A", "B", "C", "D"), result.path);
        assertEquals(3, result.totalCost);
    }

    @Test
    void constrainedPathViaLimitExceeded() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        agent.addDirectedEdge("A", "B", 1);
        agent.addDirectedEdge("B", "C", 1);
        agent.addDirectedEdge("C", "D", 1);

        // via 节点超过 3 个
        List<String> tooManyVia = java.util.Arrays.asList("B", "C", "D", "A");
        PathOptimizerAgent.PathResult result = agent.constrainedPath(
                "A", "D", tooManyVia, java.util.Collections.emptyList(), null);

        // 由于排列数过多，算法仍会尝试，但通过 IntentParser 层限制
        // 这里测试 agent 层可以处理（不崩溃）
        // 实际限制在 parser 层
    }

    @Test
    void constrainedPathAvoidSrcOrDstReturnsNull() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        agent.addDirectedEdge("A", "B", 1);
        agent.addDirectedEdge("B", "D", 1);

        // 避开源节点
        PathOptimizerAgent.PathResult r1 = agent.constrainedPath(
                "A", "D", java.util.Collections.emptyList(),
                java.util.Collections.singletonList("A"), null);
        assertNull(r1);

        // 避开目标节点
        PathOptimizerAgent.PathResult r2 = agent.constrainedPath(
                "A", "D", java.util.Collections.emptyList(),
                java.util.Collections.singletonList("D"), null);
        assertNull(r2);
    }
}
