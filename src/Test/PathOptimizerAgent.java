package Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
        List<String> lines = Files.readAllLines(path);
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
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
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
}