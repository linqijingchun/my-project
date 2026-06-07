package Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class PathOptimizerAgent {
    private Map<String, List<Edge>> graph;

    public Map<String, List<Edge>> getGraph() {
        return graph;
    }

    public PathOptimizerAgent() {
        graph = new HashMap<>();
    }

    public void clear() {
        graph.clear();
    }

    // ---------- 图修改操作 ----------
    private void ensureNode(String node) {
        graph.putIfAbsent(node, new ArrayList<>());
    }

    private void validateWeight(int weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("权重必须是正整数");
        }
    }

    public void addDirectedEdge(String from, String to, int weight) {
        validateWeight(weight);
        ensureNode(from);
        ensureNode(to);
        for (Edge e : graph.get(from)) {
            if (e.target.equals(to)) {
                throw new IllegalArgumentException(
                        "边已存在: " + from + " -> " + to + " (当前权重 " + e.weight + ")，如需修改请使用 update 命令");
            }
        }
        graph.get(from).add(new Edge(to, weight));
    }

    public void addUndirectedEdge(String a, String b, int weight) {
        addDirectedEdge(a, b, weight);
        addDirectedEdge(b, a, weight);
    }

    public void updateWeight(String from, String to, int newWeight) {
        validateWeight(newWeight);
        if (!graph.containsKey(from) || !graph.containsKey(to)) {
            throw new IllegalArgumentException("节点不存在: " + from + " 或 " + to);
        }
        List<Edge> edges = graph.get(from);
        for (Edge edge : edges) {
            if (edge.target.equals(to)) {
                edge.weight = newWeight;
                return;
            }
        }
        throw new IllegalArgumentException("边不存在: " + from + " -> " + to);
    }

    public void removeEdge(String from, String to) {
        if (!graph.containsKey(from)) return;
        graph.get(from).removeIf(e -> e.target.equals(to));
    }

    // ---------- 最短路径算法 ----------
    // 结果封装类：作为智能体的内部类
    public static class PathResult {
        public final List<String> path;
        public final int totalCost;
        public PathResult(List<String> path, int totalCost) {
            this.path = Collections.unmodifiableList(path); // 不可变
            this.totalCost = totalCost;
        }
    }

    private PathResult dijkstra(String src, String dst) {
        if (!graph.containsKey(src) || !graph.containsKey(dst)) {
            return null;
        }

        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        for (String node : graph.keySet()) {
            dist.put(node, Integer.MAX_VALUE);
            prev.put(node, null);
        }
        dist.put(src, 0);

        PriorityQueue<NodeDist> pq = new PriorityQueue<>();
        pq.offer(new NodeDist(src, 0));

        while (!pq.isEmpty()) {
            NodeDist current = pq.poll();
            String u = current.node;
            int d = current.dist;
            if (d != dist.get(u)) continue;
            if (u.equals(dst)) break;

            for (Edge e : graph.get(u)) {
                String v = e.target;
                int newDist = d + e.weight;
                if (newDist < dist.get(v)) {
                    dist.put(v, newDist);
                    prev.put(v, u);
                    pq.offer(new NodeDist(v, newDist));
                }
            }
        }

        if (dist.get(dst) == Integer.MAX_VALUE) {
            return null;
        }

        List<String> path = new ArrayList<>();
        for (String at = dst; at != null; at = prev.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return new PathResult(path, dist.get(dst));
    }

    public PathResult shortestPath(String src, String dst) {
        return dijkstra(src, dst);
    }

    public boolean isReachable(String src, String dst) {
        return shortestPath(src, dst) != null;
    }

    // ---------- 辅助显示 ----------
    public void showGraph() {
        if (graph.isEmpty()) {
            System.out.println("图为空");
            return;
        }
        for (Map.Entry<String, List<Edge>> entry : graph.entrySet()) {
            String node = entry.getKey();
            List<Edge> edges = entry.getValue();
            if (edges.isEmpty()) {
                System.out.println(node + " --> (无出边)");
            } else {
                System.out.print(node + " --> ");
                for (Edge e : edges) {
                    System.out.print(e + " ");
                }
                System.out.println();
            }
        }
    }

    /**
     * 从文件导入拓扑（清空当前图）
     */
    public void loadFromFile(String filename) throws IOException {
        // 清空现有图
        graph.clear();
        Path path = Paths.get(filename);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split("\\s+");
            if (parts.length != 3) {
                System.err.println("忽略无效行: " + line);
                continue;
            }
            String from = parts[0];
            String to = parts[1];
            int weight;
            try {
                weight = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                System.err.println("权重不是整数: " + line);
                continue;
            }
            try {
                addDirectedEdge(from, to, weight);
            } catch (IllegalArgumentException e) {
                System.err.println("忽略无效行: " + line + "，" + e.getMessage());
            }
        }
    }

    /**
     * 将当前拓扑保存到文件（有向边的格式）
     */
    public void saveToFile(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8))) {
            writer.println("# 有向图拓扑文件，格式: from to weight");
            for (Map.Entry<String, List<Edge>> entry : graph.entrySet()) {
                String from = entry.getKey();
                for (Edge e : entry.getValue()) {
                    writer.printf("%s %s %d%n", from, e.target, e.weight);
                }
            }
        }
    }

    // ---------- 多条最短路径 ----------
    public List<PathResult> getAllShortestPaths(String src, String dst) {
        if (!graph.containsKey(src) || !graph.containsKey(dst))
            return Collections.emptyList();

        Map<String, Integer> dist = new HashMap<>();
        Map<String, List<String>> prevList = new HashMap<>();
        for (String node : graph.keySet()) {
            dist.put(node, Integer.MAX_VALUE);
            prevList.put(node, new ArrayList<>());
        }
        dist.put(src, 0);

        PriorityQueue<NodeDist> pq = new PriorityQueue<>();
        pq.offer(new NodeDist(src, 0));

        while (!pq.isEmpty()) {
            NodeDist cur = pq.poll();
            String u = cur.node;
            int d = cur.dist;
            if (d != dist.get(u)) continue;
            for (Edge e : graph.get(u)) {
                String v = e.target;
                int nd = d + e.weight;
                if (nd < dist.get(v)) {
                    dist.put(v, nd);
                    prevList.get(v).clear();
                    prevList.get(v).add(u);
                    pq.offer(new NodeDist(v, nd));
                } else if (nd == dist.get(v)) {
                    prevList.get(v).add(u);
                    // 依然要将 v 加入队列，使它能继续松弛其邻居（因为可能产生新的等长路径）
                    pq.offer(new NodeDist(v, nd));
                }
            }
        }

        if (dist.get(dst) == Integer.MAX_VALUE) return Collections.emptyList();

        List<List<String>> paths = new ArrayList<>();
        backtrack(src, dst, prevList, new ArrayList<>(), paths);
        List<PathResult> results = new ArrayList<>();
        int cost = dist.get(dst);
        for (List<String> p : paths) {
            results.add(new PathResult(p, cost));
        }
        return results;
    }

    private void backtrack(String src, String node, Map<String, List<String>> prevList,
                           List<String> current, List<List<String>> result) {
        current.add(node);
        if (node.equals(src)) {
            List<String> path = new ArrayList<>(current);
            Collections.reverse(path);
            result.add(path);
            current.remove(current.size() - 1);
            return;
        }
        for (String prev : prevList.get(node)) {
            backtrack(src, prev, prevList, current, result);
        }
        current.remove(current.size() - 1);
    }

    // ---------- 拓扑分析：关键节点 + 瓶颈链路 ----------
    public TopologyAnalysisResult analyzeTopology() {
        if (graph.isEmpty()) {
            return new TopologyAnalysisResult(Collections.emptyList(), Collections.emptyList());
        }

        // 收集所有节点对的最短路径
        List<String> nodes = new ArrayList<>(graph.keySet());
        Map<String, Integer> nodePassCount = new HashMap<>();
        Map<String, Integer> edgePassCount = new HashMap<>();
        for (String node : nodes) {
            nodePassCount.put(node, 0);
        }

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (i == j) continue;
                PathResult result = dijkstra(nodes.get(i), nodes.get(j));
                if (result == null) continue;
                // 统计路径上的节点
                for (String n : result.path) {
                    nodePassCount.put(n, nodePassCount.get(n) + 1);
                }
                // 统计路径上的边
                for (int k = 0; k < result.path.size() - 1; k++) {
                    String key = result.path.get(k) + "->" + result.path.get(k + 1);
                    edgePassCount.put(key, edgePassCount.getOrDefault(key, 0) + 1);
                }
            }
        }

        // 关键节点：按经过次数排序，取前3
        List<String> criticalNodes = new ArrayList<>(nodePassCount.keySet());
        criticalNodes.sort((a, b) -> nodePassCount.get(b) - nodePassCount.get(a));
        List<String> topCritical = new ArrayList<>(criticalNodes.subList(0, Math.min(3, criticalNodes.size())));

        // 瓶颈链路：取权重最高且经过次数较多的前3条边
        List<String> bottleneckEdges = new ArrayList<>();
        List<int[]> bottleneckInfo = new ArrayList<>(); // [weight, passCount]
        for (Map.Entry<String, List<Edge>> entry : graph.entrySet()) {
            String from = entry.getKey();
            for (Edge e : entry.getValue()) {
                String key = from + "->" + e.target;
                int pass = edgePassCount.getOrDefault(key, 0);
                bottleneckEdges.add(key);
                bottleneckInfo.add(new int[]{e.weight, pass});
            }
        }
        // 按 weight * passCount 综合排序
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < bottleneckEdges.size(); i++) indices.add(i);
        indices.sort((a, b) -> {
            int scoreA = bottleneckInfo.get(a)[0] * bottleneckInfo.get(a)[1];
            int scoreB = bottleneckInfo.get(b)[0] * bottleneckInfo.get(b)[1];
            return scoreB - scoreA;
        });
        List<String> topBottleneck = new ArrayList<>();
        for (int i = 0; i < Math.min(3, indices.size()); i++) {
            int idx = indices.get(i);
            String edge = bottleneckEdges.get(idx);
            int weight = bottleneckInfo.get(idx)[0];
            int pass = bottleneckInfo.get(idx)[1];
            topBottleneck.add(edge + " (权重" + weight + ", 经过" + pass + "次)");
        }

        return new TopologyAnalysisResult(topCritical, topBottleneck);
    }

    // ---------- 约束路径查询 ----------

    /**
     * 带约束的路径查询
     * @param src 源节点
     * @param dst 目标节点
     * @param viaNodes 必经节点列表（最多3个）
     * @param avoidNodes 避开节点列表
     * @param maxHops 最大跳数限制
     * @return 最优约束路径，不可达返回 null
     */
    public PathResult constrainedPath(String src, String dst,
                                       List<String> viaNodes, List<String> avoidNodes,
                                       Integer maxHops) {
        if (!graph.containsKey(src) || !graph.containsKey(dst)) {
            return null;
        }

        // 检查 via/avoid 节点是否存在
        for (String node : viaNodes) {
            if (!graph.containsKey(node)) return null;
        }
        for (String node : avoidNodes) {
            if (!graph.containsKey(node)) return null;
        }
        // src/dst 不能在 avoid 列表中
        if (avoidNodes.contains(src) || avoidNodes.contains(dst)) return null;
        // via 节点不能和 avoid 节点重叠
        for (String v : viaNodes) {
            if (avoidNodes.contains(v)) return null;
        }

        // 纯跳数限制（无 via/avoid）
        if (viaNodes.isEmpty() && avoidNodes.isEmpty() && maxHops != null) {
            return dijkstraWithHopLimit(src, dst, maxHops);
        }

        // 纯避开节点（无 via，无 hops）
        if (viaNodes.isEmpty() && maxHops == null && !avoidNodes.isEmpty()) {
            return dijkstraWithAvoid(src, dst, avoidNodes);
        }

        // 纯必经节点（无 avoid，无 hops）或同时有 via + avoid/hops
        if (!viaNodes.isEmpty()) {
            return dijkstraWithVia(src, dst, viaNodes, avoidNodes, maxHops);
        }

        // 有 avoid + hops 组合
        if (!avoidNodes.isEmpty() && maxHops != null) {
            // 先避开节点，再限制跳数
            PathResult result = dijkstraWithAvoidAndHops(src, dst, avoidNodes, maxHops);
            return result;
        }

        return null;
    }

    private PathResult dijkstraWithHopLimit(String src, String dst, int maxHops) {
        // 状态空间 Dijkstra：dist[node][hops]
        Map<String, int[]> dist = new HashMap<>();
        Map<String, String[]> prev = new HashMap<>();
        for (String node : graph.keySet()) {
            dist.put(node, new int[maxHops + 1]);
            prev.put(node, new String[maxHops + 1]);
            for (int h = 0; h <= maxHops; h++) {
                dist.get(node)[h] = Integer.MAX_VALUE;
            }
        }
        dist.get(src)[0] = 0;

        // State: (node, hops, cost)
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[2] - b[2]);
        pq.offer(new int[]{nodeToIndex(src), 0, 0});

        List<String> nodeList = new ArrayList<>(graph.keySet());
        Map<String, Integer> nodeIndex = new HashMap<>();
        for (int i = 0; i < nodeList.size(); i++) nodeIndex.put(nodeList.get(i), i);

        // 重新用字符串优先队列
        // State: node, hops, cost
        List<String> stateNodes = new ArrayList<>();
        List<Integer> stateHops = new ArrayList<>();

        // 简化：用 map 记录最优
        Map<String, Integer> bestDist = new HashMap<>(); // "node|hops" -> cost
        Map<String, String> bestPrev = new HashMap<>();  // "node|hops" -> prevNode
        Map<String, Integer> bestPrevHops = new HashMap<>();

        String startKey = src + "|0";
        bestDist.put(startKey, 0);

        // 优先队列元素: [cost, nodeIndex, hops]
        List<int[]> queue = new ArrayList<>();
        queue.add(new int[]{0, 0, 0}); // cost, dummy, hops
        List<String> queueNode = new ArrayList<>();
        queueNode.add(src);

        // 用简单列表+排序模拟优先队列
        List<Integer> costs = new ArrayList<>();
        List<String> nodes = new ArrayList<>();
        List<Integer> hopsList = new ArrayList<>();
        costs.add(0);
        nodes.add(src);
        hopsList.add(0);

        while (!costs.isEmpty()) {
            // 找最小
            int minIdx = 0;
            for (int i = 1; i < costs.size(); i++) {
                if (costs.get(i) < costs.get(minIdx)) minIdx = i;
            }
            int d = costs.get(minIdx);
            String u = nodes.get(minIdx);
            int h = hopsList.get(minIdx);
            costs.remove(minIdx);
            nodes.remove(minIdx);
            hopsList.remove(minIdx);

            String uKey = u + "|" + h;
            if (d != bestDist.getOrDefault(uKey, Integer.MAX_VALUE)) continue;
            if (u.equals(dst)) {
                // 回溯路径
                return reconstructHopPath(src, dst, h, bestPrev, bestPrevHops);
            }

            if (h >= maxHops) continue;

            for (Edge e : graph.get(u)) {
                String v = e.target;
                int newDist = d + e.weight;
                int newHops = h + 1;
                String vKey = v + "|" + newHops;
                if (newDist < bestDist.getOrDefault(vKey, Integer.MAX_VALUE)) {
                    bestDist.put(vKey, newDist);
                    bestPrev.put(vKey, u);
                    bestPrevHops.put(vKey, h);
                    costs.add(newDist);
                    nodes.add(v);
                    hopsList.add(newHops);
                }
            }
        }
        return null;
    }

    private PathResult reconstructHopPath(String src, String dst, int hops,
                                           Map<String, String> bestPrev,
                                           Map<String, Integer> bestPrevHops) {
        List<String> path = new ArrayList<>();
        String node = dst;
        int h = hops;
        while (node != null) {
            path.add(node);
            String key = node + "|" + h;
            String prevNode = bestPrev.get(key);
            Integer prevHop = bestPrevHops.get(key);
            if (prevNode == null) break;
            node = prevNode;
            h = prevHop;
        }
        Collections.reverse(path);
        // 计算总代价
        int totalCost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            for (Edge e : graph.get(path.get(i))) {
                if (e.target.equals(path.get(i + 1))) {
                    totalCost += e.weight;
                    break;
                }
            }
        }
        return new PathResult(path, totalCost);
    }

    private int nodeToIndex(String node) {
        return 0; // placeholder, not used in final impl
    }

    private PathResult dijkstraWithAvoid(String src, String dst, List<String> avoidNodes) {
        // 临时移除 avoid 节点
        Map<String, List<Edge>> removed = new HashMap<>();
        for (String node : avoidNodes) {
            if (graph.containsKey(node)) {
                removed.put(node, new ArrayList<>(graph.get(node)));
                graph.remove(node);
            }
        }
        // 移除指向 avoid 节点的边
        Map<String, List<Edge>> removedIncoming = new HashMap<>();
        for (String node : avoidNodes) {
            for (Map.Entry<String, List<Edge>> entry : graph.entrySet()) {
                String from = entry.getKey();
                List<Edge> edges = entry.getValue();
                List<Edge> toRemove = new ArrayList<>();
                for (Edge e : edges) {
                    if (e.target.equals(node)) toRemove.add(e);
                }
                if (!toRemove.isEmpty()) {
                    removedIncoming.computeIfAbsent(from, k -> new ArrayList<>()).addAll(toRemove);
                    edges.removeAll(toRemove);
                }
            }
        }

        PathResult result = dijkstra(src, dst);

        // 恢复图
        for (Map.Entry<String, List<Edge>> entry : removed.entrySet()) {
            graph.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, List<Edge>> entry : removedIncoming.entrySet()) {
            graph.get(entry.getKey()).addAll(entry.getValue());
        }

        return result;
    }

    private PathResult dijkstraWithVia(String src, String dst,
                                        List<String> viaNodes, List<String> avoidNodes,
                                        Integer maxHops) {
        // 尝试所有 via 节点排列，找总代价最小的
        List<List<String>> permutations = permute(viaNodes);
        PathResult best = null;

        for (List<String> order : permutations) {
            // 构建完整路径: src -> via[0] -> via[1] -> ... -> dst
            List<String> waypoints = new ArrayList<>();
            waypoints.add(src);
            waypoints.addAll(order);
            waypoints.add(dst);

            boolean valid = true;
            List<String> fullPath = new ArrayList<>();
            int totalCost = 0;

            for (int i = 0; i < waypoints.size() - 1; i++) {
                PathResult segment;
                if (!avoidNodes.isEmpty()) {
                    segment = dijkstraWithAvoid(waypoints.get(i), waypoints.get(i + 1), avoidNodes);
                } else if (maxHops != null) {
                    // 每段分配合理的跳数上限
                    int segHops = Math.max(maxHops / (waypoints.size() - 1), 1);
                    segment = dijkstraWithHopLimit(waypoints.get(i), waypoints.get(i + 1), segHops);
                } else {
                    segment = dijkstra(waypoints.get(i), waypoints.get(i + 1));
                }
                if (segment == null) {
                    valid = false;
                    break;
                }
                // 拼接路径（去掉重复节点）
                if (i == 0) {
                    fullPath.addAll(segment.path);
                } else {
                    fullPath.addAll(segment.path.subList(1, segment.path.size()));
                }
                totalCost += segment.totalCost;
            }

            if (valid && (best == null || totalCost < best.totalCost)) {
                best = new PathResult(fullPath, totalCost);
            }
        }

        // 如果有 maxHops 约束，验证总跳数
        if (best != null && maxHops != null && best.path.size() - 1 > maxHops) {
            // 尝试全局跳数限制
            PathResult globalResult = dijkstraWithHopLimitAndAvoid(src, dst, avoidNodes, maxHops);
            if (globalResult != null) return globalResult;
            return null;
        }

        return best;
    }

    private PathResult dijkstraWithHopLimitAndAvoid(String src, String dst,
                                                     List<String> avoidNodes, int maxHops) {
        // 先避开节点，再限制跳数
        Map<String, List<Edge>> removed = new HashMap<>();
        for (String node : avoidNodes) {
            if (graph.containsKey(node)) {
                removed.put(node, new ArrayList<>(graph.get(node)));
                graph.remove(node);
            }
        }
        Map<String, List<Edge>> removedIncoming = new HashMap<>();
        for (String node : avoidNodes) {
            for (Map.Entry<String, List<Edge>> entry : graph.entrySet()) {
                String from = entry.getKey();
                List<Edge> edges = entry.getValue();
                List<Edge> toRemove = new ArrayList<>();
                for (Edge e : edges) {
                    if (e.target.equals(node)) toRemove.add(e);
                }
                if (!toRemove.isEmpty()) {
                    removedIncoming.computeIfAbsent(from, k -> new ArrayList<>()).addAll(toRemove);
                    edges.removeAll(toRemove);
                }
            }
        }

        PathResult result = dijkstraWithHopLimit(src, dst, maxHops);

        // 恢复图
        for (Map.Entry<String, List<Edge>> entry : removed.entrySet()) {
            graph.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, List<Edge>> entry : removedIncoming.entrySet()) {
            graph.get(entry.getKey()).addAll(entry.getValue());
        }

        return result;
    }

    private PathResult dijkstraWithAvoidAndHops(String src, String dst,
                                                 List<String> avoidNodes, int maxHops) {
        return dijkstraWithHopLimitAndAvoid(src, dst, avoidNodes, maxHops);
    }

    private List<List<String>> permute(List<String> list) {
        List<List<String>> result = new ArrayList<>();
        if (list.isEmpty()) {
            result.add(new ArrayList<>());
            return result;
        }
        boolean[] used = new boolean[list.size()];
        backtrackPermute(list, new ArrayList<>(), used, result);
        return result;
    }

    private void backtrackPermute(List<String> list, List<String> current,
                                   boolean[] used, List<List<String>> result) {
        if (current.size() == list.size()) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            if (used[i]) continue;
            used[i] = true;
            current.add(list.get(i));
            backtrackPermute(list, current, used, result);
            current.remove(current.size() - 1);
            used[i] = false;
        }
    }

    public static class TopologyAnalysisResult {
        public final List<String> criticalNodes;
        public final List<String> bottleneckEdges;
        public TopologyAnalysisResult(List<String> criticalNodes, List<String> bottleneckEdges) {
            this.criticalNodes = Collections.unmodifiableList(criticalNodes);
            this.bottleneckEdges = Collections.unmodifiableList(bottleneckEdges);
        }
    }

    public TopologySummary summarizeTopology() {
        int edgeCount = 0;
        Map<String, Integer> inDegree = new HashMap<>();

        for (String node : graph.keySet()) {
            inDegree.put(node, 0);
        }

        for (Map.Entry<String, List<Edge>> entry : graph.entrySet()) {
            List<Edge> edges = entry.getValue();
            edgeCount += edges.size();

            for (Edge edge : edges) {
                inDegree.put(edge.target, inDegree.getOrDefault(edge.target, 0) + 1);
            }
        }

        List<String> isolatedNodes = new ArrayList<>();
        List<String> noOutgoingNodes = new ArrayList<>();

        for (Map.Entry<String, List<Edge>> entry : graph.entrySet()) {
            String node = entry.getKey();
            boolean noIncoming = inDegree.getOrDefault(node, 0) == 0;
            boolean noOutgoing = entry.getValue().isEmpty();

            if (noIncoming && noOutgoing) {
                isolatedNodes.add(node);
            }

            if (noOutgoing) {
                noOutgoingNodes.add(node);
            }
        }

        Collections.sort(isolatedNodes);
        Collections.sort(noOutgoingNodes);

        return new TopologySummary(
                graph.size(),
                edgeCount,
                isolatedNodes,
                noOutgoingNodes
        );
    }
}