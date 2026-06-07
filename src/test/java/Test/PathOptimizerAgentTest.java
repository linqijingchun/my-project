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

    // ---------- 链路指标多维扩展测试 ----------

    @Test
    void linkMetricsGetCostByStrategy() {
        LinkMetrics metrics = new LinkMetrics(10, 100, 0.5, 99.9);

        assertEquals(10, metrics.getCost(OptimizeStrategy.DELAY));
        assertEquals(LinkMetrics.MAX_BANDWIDTH - 100, metrics.getCost(OptimizeStrategy.BANDWIDTH));
        assertEquals(50, metrics.getCost(OptimizeStrategy.PACKET_LOSS));   // 0.5 * 100
        assertEquals(9, metrics.getCost(OptimizeStrategy.RELIABILITY));    // (100-99.9)*100 ≈ 9.99 → (int)=9
    }

    @Test
    void edgeGetCostWithNullMetrics() {
        Edge edge = new Edge("B", 5);
        assertEquals(5, edge.getCost(OptimizeStrategy.WEIGHT));
        assertEquals(5, edge.getCost(OptimizeStrategy.DELAY)); // null metrics → fallback to weight
    }

    @Test
    void edgeGetCostWithMetrics() {
        LinkMetrics metrics = new LinkMetrics(20, 500, 1.0, 99.0);
        Edge edge = new Edge("B", 5, metrics);

        assertEquals(5, edge.getCost(OptimizeStrategy.WEIGHT));
        assertEquals(20, edge.getCost(OptimizeStrategy.DELAY));
    }

    @Test
    void strategyAffectsShortestPath() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        // A->B: 低时延(5ms) 高权重(10)
        // A->C->B: 高时延(20+20=40ms) 低权重(1+1=2)
        agent.addDirectedEdge("A", "B", 10, new LinkMetrics(5, 100, 0, 100));
        agent.addDirectedEdge("A", "C", 1, new LinkMetrics(20, 100, 0, 100));
        agent.addDirectedEdge("C", "B", 1, new LinkMetrics(20, 100, 0, 100));

        // WEIGHT 策略：走 A->C->B（代价2）
        agent.setStrategy(OptimizeStrategy.WEIGHT);
        PathOptimizerAgent.PathResult r1 = agent.shortestPath("A", "B");
        assertNotNull(r1);
        assertEquals(2, r1.totalCost);

        // DELAY 策略：走 A->B（时延5）
        agent.setStrategy(OptimizeStrategy.DELAY);
        PathOptimizerAgent.PathResult r2 = agent.shortestPath("A", "B");
        assertNotNull(r2);
        assertEquals(5, r2.totalCost);
    }

    @Test
    void addEdgeWithMetricsAndSaveLoad() throws java.io.IOException {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        agent.addDirectedEdge("A", "B", 5, new LinkMetrics(10, 100, 0.5, 99.9));

        String tmpFile = "test_metrics_topology.txt";
        agent.saveToFile(tmpFile);

        PathOptimizerAgent agent2 = new PathOptimizerAgent();
        agent2.loadFromFile(tmpFile);

        // 验证加载后 metrics 保留
        Edge loadedEdge = agent2.getGraph().get("A").get(0);
        assertNotNull(loadedEdge.metrics);
        assertEquals(10, loadedEdge.metrics.delay);
        assertEquals(100, loadedEdge.metrics.bandwidth);
        assertEquals(0.5, loadedEdge.metrics.packetLoss, 0.001);
        assertEquals(99.9, loadedEdge.metrics.reliability, 0.001);

        // 清理
        new java.io.File(tmpFile).delete();
    }

    @Test
    void addEdgeWithoutMetricsSaveAsOldFormat() throws java.io.IOException {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        agent.addDirectedEdge("A", "B", 5);

        String tmpFile = "test_old_topology.txt";
        agent.saveToFile(tmpFile);

        // 读取文件内容验证是3列格式
        java.util.List<String> lines = java.nio.file.Files.readAllLines(
                java.nio.file.Paths.get(tmpFile), java.nio.charset.StandardCharsets.UTF_8);
        boolean foundOldFormat = false;
        for (String line : lines) {
            if (line.startsWith("A") && line.split("\\s+").length == 3) {
                foundOldFormat = true;
                break;
            }
        }
        assertTrue(foundOldFormat, "无指标边应保存为3列格式");

        new java.io.File(tmpFile).delete();
    }

    @Test
    void linkMetricsValidation() {
        assertThrows(IllegalArgumentException.class, () -> new LinkMetrics(0, 100, 0, 100));
        assertThrows(IllegalArgumentException.class, () -> new LinkMetrics(10, -1, 0, 100));
        assertThrows(IllegalArgumentException.class, () -> new LinkMetrics(10, 100, -1, 100));
        assertThrows(IllegalArgumentException.class, () -> new LinkMetrics(10, 100, 0, 101));
    }

    // ---------- K 条最短路径测试 ----------

    @Test
    void yenK3Paths() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        // A->B->D 代价3, A->C->D 代价3, A->B->E->D 代价4, A->C->B->D 代价7
        agent.addDirectedEdge("A", "B", 1);
        agent.addDirectedEdge("B", "D", 2);
        agent.addDirectedEdge("A", "C", 1);
        agent.addDirectedEdge("C", "D", 2);
        agent.addDirectedEdge("B", "E", 1);
        agent.addDirectedEdge("E", "D", 2);
        agent.addDirectedEdge("C", "B", 4);

        java.util.List<PathOptimizerAgent.PathResult> results = agent.yenKShortestPaths("A", "D", 3);

        assertEquals(3, results.size());
        // 第1、2条代价相同(3)，第3条代价4
        assertEquals(3, results.get(0).totalCost);
        assertEquals(3, results.get(1).totalCost);
        assertEquals(4, results.get(2).totalCost);
        // 路径不重复
        assertNotEquals(results.get(0).path, results.get(1).path);
    }

    @Test
    void yenKExceedsAvailable() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        agent.addDirectedEdge("A", "B", 1);
        agent.addDirectedEdge("B", "D", 1);

        // 只有1条路径，请求K=5
        java.util.List<PathOptimizerAgent.PathResult> results = agent.yenKShortestPaths("A", "D", 5);

        assertEquals(1, results.size());
        assertEquals(java.util.Arrays.asList("A", "B", "D"), results.get(0).path);
    }

    @Test
    void yenK1EqualsDijkstra() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        agent.addDirectedEdge("A", "B", 3);
        agent.addDirectedEdge("B", "D", 2);
        agent.addDirectedEdge("A", "C", 1);
        agent.addDirectedEdge("C", "D", 1);

        PathOptimizerAgent.PathResult dijkstraResult = agent.shortestPath("A", "D");
        java.util.List<PathOptimizerAgent.PathResult> yenResults = agent.yenKShortestPaths("A", "D", 1);

        assertEquals(1, yenResults.size());
        assertEquals(dijkstraResult.path, yenResults.get(0).path);
        assertEquals(dijkstraResult.totalCost, yenResults.get(0).totalCost);
    }

    @Test
    void yenKUnreachable() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        agent.addDirectedEdge("A", "B", 1);
        agent.addDirectedEdge("C", "D", 1);

        java.util.List<PathOptimizerAgent.PathResult> results = agent.yenKShortestPaths("A", "D", 3);

        assertTrue(results.isEmpty());
    }

    @Test
    void yenKPathOrderAscending() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        agent.addDirectedEdge("A", "B", 1);
        agent.addDirectedEdge("B", "D", 1);
        agent.addDirectedEdge("A", "C", 2);
        agent.addDirectedEdge("C", "D", 2);

        java.util.List<PathOptimizerAgent.PathResult> results = agent.yenKShortestPaths("A", "D", 5);

        // 验证代价升序
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).totalCost <= results.get(i + 1).totalCost,
                    "路径代价应按升序排列");
        }
    }
}
