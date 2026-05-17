package Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

// ---------- 辅助数据结构 ----------
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

// ---------- 智能体核心 ----------
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

// ---------- 程序入口 ----------
public class Main {
    public static void main(String[] args) {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        Scanner scanner = new Scanner(System.in);
        System.out.println("欢迎使用网络路径寻优智能体");
        System.out.println("命令列表：");
        System.out.println("  add <from> <to> <weight>        - 添加有向边");
        System.out.println("  addud <from> <to> <weight>      - 添加无向边");
        System.out.println("  path <src> <dst>                - 查询最短路径");
        System.out.println("  reach <src> <dst>               - 判断是否可达");
        System.out.println("  update <from> <to> <weight>     - 修改边的权重");
        System.out.println("  remove <from> <to>              - 删除一条边");
        System.out.println("  show                            - 显示当前图");
        System.out.println("  load <filename>                   - 从文件加载拓扑");
        System.out.println("  save <filename>                   - 保存拓扑到文件");
        System.out.println("  allpaths <src> <dst>              - 查询所有最短路径");
        System.out.println("  exit                            - 退出程序");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("exit")) {
                break;
            }
            String[] parts = line.split("\\s+");
            if (parts.length == 0) continue;

            String cmd = parts[0].toLowerCase();
            try {
                switch (cmd) {
                    case "add":
                        if (parts.length != 4) throw new IllegalArgumentException("用法: add from to weight");
                        agent.addDirectedEdge(parts[1], parts[2], Integer.parseInt(parts[3]));
                        System.out.println("已添加有向边 " + parts[1] + " -> " + parts[2] + " 权重 " + parts[3]);
                        break;
                    case "addud":
                        if (parts.length != 4) throw new IllegalArgumentException("用法: addud from to weight");
                        agent.addUndirectedEdge(parts[1], parts[2], Integer.parseInt(parts[3]));
                        System.out.println("已添加无向边 " + parts[1] + " <-> " + parts[2] + " 权重 " + parts[3]);
                        break;
                    case "path":
                        if (parts.length != 3) throw new IllegalArgumentException("用法: path src dst");
                        PathOptimizerAgent.PathResult result = agent.shortestPath(parts[1], parts[2]);
                        if (result == null) {
                            System.out.println(parts[1] + " 到 " + parts[2] + " 不可达");
                        } else {
                            System.out.println("最短路径: " + String.join(" -> ", result.path));
                            System.out.println("总代价: " + result.totalCost);
                        }
                        break;
                    case "reach":
                        if (parts.length != 3) throw new IllegalArgumentException("用法: reach src dst");
                        boolean reachable = agent.isReachable(parts[1], parts[2]);
                        System.out.println(parts[1] + " 到 " + parts[2] + (reachable ? " 可达" : " 不可达"));
                        break;
                    case "update":
                        if (parts.length != 4) throw new IllegalArgumentException("用法: update from to newWeight");
                        agent.updateWeight(parts[1], parts[2], Integer.parseInt(parts[3]));
                        System.out.println("已更新边 " + parts[1] + " -> " + parts[2] + " 权重为 " + parts[3]);
                        break;
                    case "remove":
                        if (parts.length != 3) throw new IllegalArgumentException("用法: remove from to");
                        agent.removeEdge(parts[1], parts[2]);
                        System.out.println("已删除边 " + parts[1] + " -> " + parts[2]);
                        break;
                    case "show":
                        agent.showGraph();
                        break;
                    case "load":
                        if (parts.length != 2) throw new IllegalArgumentException("用法: load <filename>");
                        try {
                            agent.loadFromFile(parts[1]);
                            System.out.println("已从 " + parts[1] + " 加载拓扑");
                        } catch (IOException e) {
                            System.out.println("加载失败: " + e.getMessage());
                        }
                        break;
                    case "save":
                        if (parts.length != 2) throw new IllegalArgumentException("用法: save <filename>");
                        try {
                            agent.saveToFile(parts[1]);
                            System.out.println("已保存到 " + parts[1]);
                        } catch (IOException e) {
                            System.out.println("保存失败: " + e.getMessage());
                        }
                        break;
                    case "allpaths":
                        if (parts.length != 3) throw new IllegalArgumentException("用法: allpaths src dst");
                        String asrc = parts[1];
                        String adst = parts[2];
                        List<PathOptimizerAgent.PathResult> allResults = agent.getAllShortestPaths(asrc, adst);
                        if (allResults.isEmpty()) {
                            System.out.println(asrc + " 到 " + adst + " 不可达");
                        } else {
                            System.out.println("找到 " + allResults.size() + " 条最短路径（总代价 " + allResults.get(0).totalCost + "）：");
                            for (int i = 0; i < allResults.size(); i++) {
                                System.out.println((i+1) + ": " + String.join(" -> ", allResults.get(i).path));
                            }
                        }
                        break;
                    default:
                        System.out.println("未知命令，请重新输入");
                }
            } catch (NumberFormatException e) {
                System.out.println("错误: 权重必须是整数");
            } catch (IllegalArgumentException e) {
                System.out.println("错误: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("错误: " + e.toString());
            }
        }
        scanner.close();
        System.out.println("智能体已退出");
    }
}
