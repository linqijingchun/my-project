package Test;

import java.util.List;
import java.util.Map;

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

        String originalInput = line;
        String normalizedCommand = intentParser.parse(line);

        String[] parts = normalizedCommand.trim().split("\\s+");
        String cmd = parts[0].toLowerCase();
        AgentResponse response;

        try {
            switch (cmd) {
                case "add":
                    response = handleAddDirectedEdge(parts);
                    break;
                case "addud":
                    response = handleAddUndirectedEdge(parts);
                    break;
                case "path":
                    response = handleShortestPath(parts);
                    break;
                case "reach":
                    response = handleReachable(parts);
                    break;
                case "update":
                    response = handleUpdateWeight(parts);
                    break;
                case "remove":
                    response = handleRemoveEdge(parts);
                    break;
                case "show":
                    response = handleShowGraph(parts);
                    break;
                case "load":
                    response = handleLoad(parts);
                    break;
                case "save":
                    response = handleSave(parts);
                    break;
                case "allpaths":
                    response = handleAllShortestPaths(parts);
                    break;
                case "explain":
                    response = handleExplain();
                    break;
                case "help":
                    response = handleHelp();
                    break;
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

        AgentResponse responseWithCommandInfo = response.withCommandInfo(originalInput, normalizedCommand);
        context.rememberCommand(originalInput, normalizedCommand, responseWithCommandInfo.isSuccess());
        return responseWithCommandInfo;
    }

    private AgentResponse handleAddDirectedEdge(String[] parts) {
        if (parts.length != 4) {
            throw new IllegalArgumentException("用法: add from to weight");
        }

        int weight = Integer.parseInt(parts[3]);
        agent.addDirectedEdge(parts[1], parts[2], weight);

        return AgentResponse.success(
                "已添加有向边 " + parts[1] + " -> " + parts[2] + " 权重 " + weight
        );
    }

    private AgentResponse handleAddUndirectedEdge(String[] parts) {
        if (parts.length != 4) {
            throw new IllegalArgumentException("用法: addud from to weight");
        }

        int weight = Integer.parseInt(parts[3]);
        agent.addUndirectedEdge(parts[1], parts[2], weight);

        return AgentResponse.success(
                "已添加无向边 " + parts[1] + " <-> " + parts[2] + " 权重 " + weight
        );
    }

    private AgentResponse handleShortestPath(String[] parts) {
        if (parts.length != 3) {
            throw new IllegalArgumentException("用法: path src dst");
        }

        String src = parts[1];
        String dst = parts[2];

        PathOptimizerAgent.PathResult result = agent.shortestPath(src, dst);
        context.rememberPath(src, dst, result);

        if (result == null) {
            return AgentResponse.error(src + " 到 " + dst + " 不可达");
        }

        String message = "最短路径: " + String.join(" -> ", result.path)
                + "\n总代价: " + result.totalCost;

        return AgentResponse.successWithPath(message, result);
    }

    AgentContext getContext() {
        return context;
    }

    private AgentResponse handleReachable(String[] parts) {
        if (parts.length != 3) {
            throw new IllegalArgumentException("用法: reach src dst");
        }

        boolean reachable = agent.isReachable(parts[1], parts[2]);

        return AgentResponse.success(
                parts[1] + " 到 " + parts[2] + (reachable ? " 可达" : " 不可达")
        );
    }

    private AgentResponse handleUpdateWeight(String[] parts) {
        if (parts.length != 4) {
            throw new IllegalArgumentException("用法: update from to newWeight");
        }

        int weight = Integer.parseInt(parts[3]);
        agent.updateWeight(parts[1], parts[2], weight);

        return AgentResponse.success(
                "已更新边 " + parts[1] + " -> " + parts[2] + " 权重为 " + weight
        );
    }

    private AgentResponse handleRemoveEdge(String[] parts) {
        if (parts.length != 3) {
            throw new IllegalArgumentException("用法: remove from to");
        }

        agent.removeEdge(parts[1], parts[2]);

        return AgentResponse.success(
                "已删除边 " + parts[1] + " -> " + parts[2]
        );
    }

    private AgentResponse handleShowGraph(String[] parts) {
        if (parts.length != 1) {
            throw new IllegalArgumentException("用法: show");
        }

        Map<String, List<Edge>> graph = agent.getGraph();

        if (graph.isEmpty()) {
            return AgentResponse.success("图为空");
        }

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, List<Edge>> entry : graph.entrySet()) {
            String node = entry.getKey();
            List<Edge> edges = entry.getValue();

            sb.append(node).append(" --> ");

            if (edges.isEmpty()) {
                sb.append("(无出边)");
            } else {
                for (Edge edge : edges) {
                    sb.append(edge).append(" ");
                }
            }

            sb.append("\n");
        }

        return AgentResponse.success(sb.toString());
    }

    private AgentResponse handleLoad(String[] parts) throws Exception {
        if (parts.length != 2) {
            throw new IllegalArgumentException("用法: load filename");
        }

        agent.loadFromFile(parts[1]);

        return AgentResponse.success("已从 " + parts[1] + " 加载拓扑");
    }

    private AgentResponse handleSave(String[] parts) throws Exception {
        if (parts.length != 2) {
            throw new IllegalArgumentException("用法: save filename");
        }

        agent.saveToFile(parts[1]);

        return AgentResponse.success("已保存到 " + parts[1]);
    }

    private AgentResponse handleAllShortestPaths(String[] parts) {
        if (parts.length != 3) {
            throw new IllegalArgumentException("用法: allpaths src dst");
        }

        String src = parts[1];
        String dst = parts[2];

        List<PathOptimizerAgent.PathResult> results = agent.getAllShortestPaths(src, dst);

        if (results.isEmpty()) {
            return AgentResponse.error(src + " 到 " + dst + " 不可达");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("找到 ")
                .append(results.size())
                .append(" 条最短路径（总代价 ")
                .append(results.get(0).totalCost)
                .append("）：\n");

        for (int i = 0; i < results.size(); i++) {
            sb.append(i + 1)
                    .append(": ")
                    .append(String.join(" -> ", results.get(i).path))
                    .append("\n");
        }

        return AgentResponse.successWithPaths(sb.toString(), results);
    }

    private AgentResponse handleExplain() {
        if (!context.hasLastPath()) {
            return AgentResponse.error("目前还没有可解释的路径查询，请先执行 path <src> <dst>。");
        }

        PathOptimizerAgent.PathResult result = context.getLastPathResult();

        String message = "最近一次查询是 "
                + context.getLastSource()
                + " 到 "
                + context.getLastTarget()
                + "。\n最短路径为 "
                + String.join(" -> ", result.path)
                + "。\n总代价为 "
                + result.totalCost
                + "。\n这是当前图中从 "
                + context.getLastSource()
                + " 到 "
                + context.getLastTarget()
                + " 的最小总代价路径。";

        return AgentResponse.successWithPath(message, result);
    }

    private AgentResponse handleHelp() {
        return AgentResponse.success(
                "网络路径寻优智能体使用指南\n"
                        + "\n"
                        + "一、标准命令\n"
                        + "  add <from> <to> <weight>        添加有向边\n"
                        + "  addud <from> <to> <weight>      添加无向边\n"
                        + "  path <src> <dst>                查询最短路径\n"
                        + "  allpaths <src> <dst>            查询所有最短路径\n"
                        + "  reach <src> <dst>               判断是否可达\n"
                        + "  update <from> <to> <weight>     修改边的权重\n"
                        + "  remove <from> <to>              删除一条边\n"
                        + "  show                            显示当前拓扑\n"
                        + "  load <filename>                 从文件加载拓扑\n"
                        + "  save <filename>                 保存拓扑到文件\n"
                        + "  explain                         解释最近一次路径查询\n"
                        + "  exit                            退出程序\n"
                        + "\n"
                        + "二、自然语言示例\n"
                        + "  添加B到D长度为1\n"
                        + "  添加无向边A到B长度为3\n"
                        + "  帮我查询A到D的最短路径\n"
                        + "  查询A到D的所有最短路径\n"
                        + "  A到D能不能到\n"
                        + "  把A到B的权重改成4\n"
                        + "  删除A到B的边\n"
                        + "  显示当前拓扑\n"
                        + "  为什么\n"
                        + "  解释一下\n"
                        + "\n"
                        + "三、说明\n"
                        + "  节点名建议使用字母、数字或下划线，例如 A、B、Router1。\n"
                        + "  权重必须是正整数。\n"
                        + "  如果自然语言无法识别，请使用标准命令。"
        );
    }
}
