package Test;

class AgentCommandService {
    private final PathOptimizerAgent agent;
    private final AgentContext context;
    private final IntentParser intentParser = new IntentParser();

    AgentCommandService(PathOptimizerAgent agent) {
        this(agent, new AgentContext());
    }

    AgentCommandService(PathOptimizerAgent agent, AgentContext context) {
        this.agent = agent;
        this.context = context;
    }

    AgentResponse handle(String line) {
        if (line == null || line.trim().isEmpty()) {
            return AgentResponse.error("请输入命令");
        }

        Intent intent = intentParser.parse(line);
        AgentResponse response;

        try {
            switch (intent.getType()) {
                case ADD_DIRECTED:
                    response = handleAddDirectedEdge(intent);
                    break;
                case ADD_UNDIRECTED:
                    response = handleAddUndirectedEdge(intent);
                    break;
                case PATH:
                    response = handleShortestPath(intent);
                    break;
                case ALLPATHS:
                    response = handleAllShortestPaths(intent);
                    break;
                case KPATH:
                    response = handleKPath(intent);
                    break;
                case REACH:
                    response = handleReachable(intent);
                    break;
                case UPDATE:
                    response = handleUpdateWeight(intent);
                    break;
                case REMOVE:
                    response = handleRemoveEdge(intent);
                    break;
                case SHOW:
                    response = handleShowGraph();
                    break;
                case LOAD:
                    response = handleLoad(intent);
                    break;
                case SAVE:
                    response = handleSave(intent);
                    break;
                case EXPLAIN:
                    response = handleExplain();
                    break;
                case HELP:
                    response = handleHelp();
                    break;
                case SUMMARY:
                    response = handleTopologySummary();
                    break;
                case ANALYZE:
                    response = handleAnalyze();
                    break;
                case CONSTRAIN:
                    response = handleConstrain(intent);
                    break;
                case STRATEGY:
                    response = handleStrategy(intent);
                    break;
                case UNKNOWN:
                default:
                    response = AgentResponse.error("未知命令，请重新输入");
                    break;
            }
        } catch (NumberFormatException e) {
            response = AgentResponse.error("权重必须是整数");
        } catch (IllegalArgumentException e) {
            response = AgentResponse.error("错误: " + e.getMessage());
        } catch (Exception e) {
            response = AgentResponse.error("错误: " + e.toString());
        }

        AgentResponse responseWithIntent = response.withIntent(intent);
        context.rememberCommand(intent, responseWithIntent.isSuccess());
        return responseWithIntent;
    }

    // ---------- 各指令处理器 ----------

    private AgentResponse handleAddDirectedEdge(Intent intent) {
        if (intent.getMetrics() != null) {
            agent.addDirectedEdge(intent.getSource(), intent.getTarget(), intent.getWeight(), intent.getMetrics());
        } else {
            agent.addDirectedEdge(intent.getSource(), intent.getTarget(), intent.getWeight());
        }
        String msg = "已添加有向边 " + intent.getSource() + " -> " + intent.getTarget()
                + " 权重 " + intent.getWeight();
        if (intent.getMetrics() != null) {
            msg += " [" + intent.getMetrics().toShortString() + "]";
        }
        return AgentResponse.success(msg);
    }

    private AgentResponse handleAddUndirectedEdge(Intent intent) {
        if (intent.getMetrics() != null) {
            agent.addUndirectedEdge(intent.getSource(), intent.getTarget(), intent.getWeight(), intent.getMetrics());
        } else {
            agent.addUndirectedEdge(intent.getSource(), intent.getTarget(), intent.getWeight());
        }
        String msg = "已添加无向边 " + intent.getSource() + " <-> " + intent.getTarget()
                + " 权重 " + intent.getWeight();
        if (intent.getMetrics() != null) {
            msg += " [" + intent.getMetrics().toShortString() + "]";
        }
        return AgentResponse.success(msg);
    }

    private AgentResponse handleShortestPath(Intent intent) {
        PathOptimizerAgent.PathResult result =
                agent.shortestPath(intent.getSource(), intent.getTarget());
        context.rememberPath(intent.getSource(), intent.getTarget(), result);

        if (result == null) {
            return AgentResponse.error(
                    intent.getSource() + " 到 " + intent.getTarget() + " 不可达");
        }

        String message = "最短路径: " + String.join(" -> ", result.path)
                + "\n总代价: " + result.totalCost;
        return AgentResponse.successWithPath(message, result);
    }

    private AgentResponse handleAllShortestPaths(Intent intent) {
        java.util.List<PathOptimizerAgent.PathResult> results =
                agent.getAllShortestPaths(intent.getSource(), intent.getTarget());

        if (results.isEmpty()) {
            return AgentResponse.error(
                    intent.getSource() + " 到 " + intent.getTarget() + " 不可达");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("共找到 ").append(results.size()).append(" 条最短路径:\n");
        for (int i = 0; i < results.size(); i++) {
            PathOptimizerAgent.PathResult r = results.get(i);
            sb.append("路径").append(i + 1).append(": ")
                    .append(String.join(" -> ", r.path))
                    .append(" 代价: ").append(r.totalCost).append("\n");
        }
        return AgentResponse.successWithPaths(sb.toString(), results);
    }

    private AgentResponse handleKPath(Intent intent) {
        java.util.List<PathOptimizerAgent.PathResult> results =
                agent.yenKShortestPaths(intent.getSource(), intent.getTarget(), intent.getK());

        if (results.isEmpty()) {
            return AgentResponse.error(
                    intent.getSource() + " 到 " + intent.getTarget() + " 不可达");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("共找到 ").append(results.size()).append(" 条路径:\n");
        for (int i = 0; i < results.size(); i++) {
            PathOptimizerAgent.PathResult r = results.get(i);
            sb.append("路径").append(i + 1).append(": ")
                    .append(String.join(" -> ", r.path))
                    .append(" 代价: ").append(r.totalCost).append("\n");
        }
        return AgentResponse.success(sb.toString());
    }

    private AgentResponse handleReachable(Intent intent) {
        boolean reachable = agent.isReachable(intent.getSource(), intent.getTarget());
        if (reachable) {
            return AgentResponse.success(
                    intent.getSource() + " 到 " + intent.getTarget() + " 可达");
        } else {
            return AgentResponse.success(
                    intent.getSource() + " 到 " + intent.getTarget() + " 不可达");
        }
    }

    private AgentResponse handleUpdateWeight(Intent intent) {
        agent.updateWeight(intent.getSource(), intent.getTarget(), intent.getWeight());
        return AgentResponse.success(
                "已将 " + intent.getSource() + " -> " + intent.getTarget()
                        + " 权重修改为 " + intent.getWeight());
    }

    private AgentResponse handleRemoveEdge(Intent intent) {
        agent.removeEdge(intent.getSource(), intent.getTarget());
        return AgentResponse.success(
                "已删除边 " + intent.getSource() + " -> " + intent.getTarget());
    }

    private AgentResponse handleShowGraph() {
        StringBuilder sb = new StringBuilder();
        java.util.Map<String, java.util.List<Edge>> graph = agent.getGraph();
        if (graph.isEmpty()) {
            return AgentResponse.success("图为空");
        }
        for (java.util.Map.Entry<String, java.util.List<Edge>> entry : graph.entrySet()) {
            String node = entry.getKey();
            java.util.List<Edge> edges = entry.getValue();
            if (edges.isEmpty()) {
                sb.append(node).append(" --> (无出边)\n");
            } else {
                sb.append(node).append(" --> ");
                for (Edge e : edges) {
                    sb.append(e).append(" ");
                }
                sb.append("\n");
            }
        }
        return AgentResponse.success(sb.toString());
    }

    private AgentResponse handleLoad(Intent intent) {
        try {
            agent.loadFromFile(intent.getFilename());
            return AgentResponse.success("已从 " + intent.getFilename() + " 加载拓扑");
        } catch (java.io.IOException e) {
            return AgentResponse.error("加载失败: " + e.getMessage());
        }
    }

    private AgentResponse handleSave(Intent intent) {
        try {
            agent.saveToFile(intent.getFilename());
            return AgentResponse.success("已保存拓扑到 " + intent.getFilename());
        } catch (java.io.IOException e) {
            return AgentResponse.error("保存失败: " + e.getMessage());
        }
    }

    private AgentResponse handleExplain() {
        if (!context.hasLastPath()) {
            return AgentResponse.error(
                    "目前还没有可解释的路径查询，请先执行 path <src> <dst>。");
        }

        PathOptimizerAgent.PathResult result = context.getLastPathResult();
        String message = "最近一次查询是 "
                + context.getLastSource() + " 到 " + context.getLastTarget() + "。\n"
                + "最短路径为 " + String.join(" -> ", result.path) + "。\n"
                + "总代价为 " + result.totalCost + "。\n"
                + "这是当前图中从 " + context.getLastSource()
                + " 到 " + context.getLastTarget() + " 的最小总代价路径。";
        return AgentResponse.successWithPath(message, result);
    }

    private AgentResponse handleHelp() {
        return AgentResponse.success(
                "网络路径寻优智能体使用指南\n\n"
                        + "一、标准命令\n"
                        + "  add <from> <to> <weight>        添加有向边\n"
                        + "  addud <from> <to> <weight>      添加无向边\n"
                        + "  path <src> <dst>                查询最短路径\n"
                        + "  allpaths <src> <dst>            查询所有最短路径\n"
                        + "  kpath <src> <dst> <K>           查询前 K 条最短路径\n"
                        + "  reach <src> <dst>               判断是否可达\n"
                        + "  update <from> <to> <weight>     修改边的权重\n"
                        + "  remove <from> <to>              删除一条边\n"
                        + "  show                            显示当前拓扑\n"
                        + "  load <filename>                 从文件加载拓扑\n"
                        + "  save <filename>                 保存拓扑到文件\n"
                        + "  explain                         解释最近一次路径查询\n"
                        + "  summary                         输出拓扑摘要\n"
                        + "  topology                        输出拓扑摘要\n"
                        + "  analyze                         分析关键节点和瓶颈链路\n"
                        + "  constrain path <src> <dst>      约束路径查询\n"
                        + "    [via <n1,n2>] [avoid <n3>] [hops <N>]\n"
                        + "  strategy <type>                 切换优化策略\n"
                        + "    (weight/delay/bandwidth/loss/reliability)\n"
                        + "  add <from> <to> <w> [指标...]   添加带指标的边\n"
                        + "    delay <ms> bandwidth <Mbps> loss <%> reliability <%>\n"
                        + "  exit                            退出程序\n\n"
                        + "二、自然语言示例\n"
                        + "  添加B到D长度为1\n"
                        + "  帮我查询A到D的最短路径\n"
                        + "  为什么\n"
                        + "  显示网络摘要\n"
                        + "  分析关键节点\n"
                        + "  从A到D经过B的最短路径\n"
                        + "  从A到D避开C的路径\n"
                        + "  从A到D跳数不超过3的路径\n\n"
                        + "三、说明\n"
                        + "  节点名建议使用字母、数字或下划线。\n"
                        + "  权重必须是正整数。\n"
                        + "  via 节点最多 3 个，多个用逗号分隔。"
        );
    }

    private AgentResponse handleAnalyze() {
        PathOptimizerAgent.TopologyAnalysisResult result = agent.analyzeTopology();
        if (result.criticalNodes.isEmpty()) {
            return AgentResponse.success("图为空，无法分析");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("拓扑分析结果:\n\n");
        sb.append("关键节点（最短路径经过次数最多）:\n");
        for (int i = 0; i < result.criticalNodes.size(); i++) {
            sb.append("  ").append(i + 1).append(". ").append(result.criticalNodes.get(i)).append("\n");
        }
        sb.append("\n瓶颈链路（权重×经过次数最高）:\n");
        if (result.bottleneckEdges.isEmpty()) {
            sb.append("  无\n");
        } else {
            for (int i = 0; i < result.bottleneckEdges.size(); i++) {
                sb.append("  ").append(i + 1).append(". ").append(result.bottleneckEdges.get(i)).append("\n");
            }
        }
        return AgentResponse.success(sb.toString());
    }

    private AgentResponse handleConstrain(Intent intent) {
        PathOptimizerAgent.PathResult result = agent.constrainedPath(
                intent.getSource(), intent.getTarget(),
                intent.getViaNodes(), intent.getAvoidNodes(), intent.getMaxHops());

        if (result == null) {
            return AgentResponse.error(
                    intent.getSource() + " 到 " + intent.getTarget() + " 在约束条件下不可达");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("约束最短路径: ").append(String.join(" -> ", result.path)).append("\n");
        sb.append("总代价: ").append(result.totalCost);
        if (!intent.getViaNodes().isEmpty()) {
            sb.append("\n必经节点: ").append(String.join(", ", intent.getViaNodes()));
        }
        if (!intent.getAvoidNodes().isEmpty()) {
            sb.append("\n避开节点: ").append(String.join(", ", intent.getAvoidNodes()));
        }
        if (intent.getMaxHops() != null) {
            sb.append("\n最大跳数: ").append(intent.getMaxHops());
            sb.append(" (实际跳数: ").append(result.path.size() - 1).append(")");
        }
        return AgentResponse.successWithPath(sb.toString(), result);
    }

    private AgentResponse handleStrategy(Intent intent) {
        agent.setStrategy(intent.getStrategy());
        String strategyName;
        switch (intent.getStrategy()) {
            case DELAY:      strategyName = "时延"; break;
            case BANDWIDTH:  strategyName = "带宽"; break;
            case PACKET_LOSS: strategyName = "丢包率"; break;
            case RELIABILITY: strategyName = "可靠性"; break;
            default:         strategyName = "综合权重"; break;
        }
        return AgentResponse.success("优化策略已切换为: " + strategyName);
    }

    private AgentResponse handleTopologySummary() {
        TopologySummary summary = agent.summarizeTopology();
        StringBuilder sb = new StringBuilder();
        sb.append("拓扑摘要:\n");
        sb.append("节点数: ").append(summary.getNodeCount()).append("\n");
        sb.append("边数: ").append(summary.getEdgeCount()).append("\n");
        sb.append("孤立节点数: ").append(summary.getIsolatedNodes().size()).append("\n");
        if (summary.getIsolatedNodes().isEmpty()) {
            sb.append("孤立节点: 无\n");
        } else {
            sb.append("孤立节点: ")
                    .append(String.join(", ", summary.getIsolatedNodes())).append("\n");
        }
        if (summary.getNoOutgoingNodes().isEmpty()) {
            sb.append("无出边节点: 无");
        } else {
            sb.append("无出边节点: ")
                    .append(String.join(", ", summary.getNoOutgoingNodes()));
        }
        return AgentResponse.success(sb.toString());
    }

    AgentContext getContext() {
        return context;
    }
}