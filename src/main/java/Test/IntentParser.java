package Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class IntentParser {

    Intent parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return Intent.unknown(input);
        }

        String text = input.trim();

        // 1. 先识别标准命令（add、path、show 等）
        Intent standard = parseStandardCommand(text);
        if (standard != null) {
            return standard;
        }

        // 2. 再识别自然语言
        Intent intent;

        intent = parseAddDirectedEdge(text, input);
        if (intent != null) return intent;

        intent = parseAddUndirectedEdge(text, input);
        if (intent != null) return intent;

        intent = parseUpdateWeight(text, input);
        if (intent != null) return intent;

        intent = parseRemoveEdge(text, input);
        if (intent != null) return intent;

        intent = parseAllShortestPaths(text, input);
        if (intent != null) return intent;

        intent = parseShortestPath(text, input);
        if (intent != null) return intent;

        intent = parseReachable(text, input);
        if (intent != null) return intent;

        intent = parseExplain(text, input);
        if (intent != null) return intent;

        intent = parseTopologySummary(text, input);
        if (intent != null) return intent;

        intent = parseShowGraph(text, input);
        if (intent != null) return intent;

        return Intent.unknown(input);
    }

    // ==================== 标准命令解析 ====================

    private Intent parseStandardCommand(String text) {
        String[] parts = text.trim().split("\\s+");
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "add":
                if (parts.length == 4) {
                    try {
                        return Intent.addDirected(text, parts[1], parts[2],
                                Integer.parseInt(parts[3]));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                break;
            case "addud":
                if (parts.length == 4) {
                    try {
                        return Intent.addUndirected(text, parts[1], parts[2],
                                Integer.parseInt(parts[3]));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                break;
            case "path":
                if (parts.length == 3) {
                    return Intent.path(text, parts[1], parts[2]);
                }
                break;
            case "allpaths":
                if (parts.length == 3) {
                    return Intent.allPaths(text, parts[1], parts[2]);
                }
                break;
            case "reach":
                if (parts.length == 3) {
                    return Intent.reach(text, parts[1], parts[2]);
                }
                break;
            case "update":
                if (parts.length == 4) {
                    try {
                        return Intent.update(text, parts[1], parts[2],
                                Integer.parseInt(parts[3]));
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                break;
            case "remove":
                if (parts.length == 3) {
                    return Intent.remove(text, parts[1], parts[2]);
                }
                break;
            case "show":
                return Intent.show(text);
            case "load":
                if (parts.length == 2) {
                    return Intent.load(text, parts[1]);
                }
                break;
            case "save":
                if (parts.length == 2) {
                    return Intent.save(text, parts[1]);
                }
                break;
            case "explain":
                return Intent.explain(text);
            case "help":
                return Intent.help(text);
            case "summary":
            case "topology":
                return Intent.summary(text);
            default:
                return null;
        }
        return null;
    }

    // ==================== 自然语言解析 ====================

    private Intent parseAddDirectedEdge(String text, String raw) {
        if (!(text.contains("添加") || text.contains("新增") || text.contains("加入"))) {
            return null;
        }
        if (text.contains("无向")) {
            return null;
        }
        String[] edge = extractEdgeWithWeight(text);
        if (edge == null) return null;
        return Intent.addDirected(raw, edge[0], edge[1], Integer.parseInt(edge[2]));
    }

    private Intent parseAddUndirectedEdge(String text, String raw) {
        if (!(text.contains("无向") || text.contains("双向") || text.contains("互通"))) {
            return null;
        }
        String[] edge = extractEdgeWithWeight(text);
        if (edge == null) return null;
        return Intent.addUndirected(raw, edge[0], edge[1], Integer.parseInt(edge[2]));
    }

    private Intent parseUpdateWeight(String text, String raw) {
        if (!(text.contains("修改") || text.contains("更新") || text.contains("改成")
                || text.contains("改为") || text.contains("变成") || text.contains("设为")
                || text.contains("设置") || text.contains("调整") || text.contains("将"))) {
            return null;
        }
        String[] edge = extractEdgeWithWeight(text);
        if (edge == null) return null;
        return Intent.update(raw, edge[0], edge[1], Integer.parseInt(edge[2]));
    }

    private Intent parseRemoveEdge(String text, String raw) {
        if (!(text.contains("删除") || text.contains("移除") || text.contains("去掉"))) {
            return null;
        }
        String[] nodes = extractTwoNodes(text);
        if (nodes == null) return null;
        return Intent.remove(raw, nodes[0], nodes[1]);
    }

    private Intent parseAllShortestPaths(String text, String raw) {
        if (!(text.contains("所有最短路径") || text.contains("全部最短路径")
                || text.contains("所有路径") || text.contains("全部路径"))) {
            return null;
        }
        String[] nodes = extractTwoNodes(text);
        if (nodes == null) return null;
        return Intent.allPaths(raw, nodes[0], nodes[1]);
    }

    private Intent parseShortestPath(String text, String raw) {
        if (!(text.contains("最短路径") || text.contains("最优路径")
                || text.contains("怎么走") || text.contains("最便宜")
                || text.contains("总代价最小"))) {
            return null;
        }
        String[] nodes = extractTwoNodes(text);
        if (nodes == null) return null;
        return Intent.path(raw, nodes[0], nodes[1]);
    }

    private Intent parseReachable(String text, String raw) {
        if (!(text.contains("可达") || text.contains("能到")
                || text.contains("能不能到") || text.contains("是否能到")
                || text.contains("能否到达"))) {
            return null;
        }
        String[] nodes = extractTwoNodes(text);
        if (nodes == null) return null;
        return Intent.reach(raw, nodes[0], nodes[1]);
    }

    private Intent parseExplain(String text, String raw) {
        if (text.equalsIgnoreCase("why") || text.contains("为什么")
                || text.contains("为啥") || text.contains("解释")
                || text.contains("原因")) {
            return Intent.explain(raw);
        }
        return null;
    }

    private Intent parseTopologySummary(String text, String raw) {
        if ((text.contains("摘要") || text.contains("概览") || text.contains("分析"))
                && (text.contains("拓扑") || text.contains("网络") || text.contains("图"))) {
            return Intent.summary(raw);
        }
        if (text.contains("当前拓扑情况") || text.contains("网络情况")
                || text.contains("拓扑情况")) {
            return Intent.summary(raw);
        }
        return null;
    }

    private Intent parseShowGraph(String text, String raw) {
        if (text.contains("显示") || text.contains("查看") || text.contains("展示")) {
            if (text.contains("图") || text.contains("拓扑") || text.contains("网络")) {
                return Intent.show(raw);
            }
        }
        return null;
    }

    // ==================== 辅助提取 ====================

    private String[] extractTwoNodes(String text) {
        Pattern pattern = Pattern.compile(
                "([A-Za-z0-9_]+)\\s*(?:到|->|至|和|与|去|往|来|从|向)\\s*([A-Za-z0-9_]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return new String[]{matcher.group(1), matcher.group(2)};
        }
        return null;
    }

    private String[] extractEdgeWithWeight(String text) {
        Pattern pattern = Pattern.compile(
                "([A-Za-z0-9_]+)\\s*(?:到|->|至|和|与|去|往|来|从|向)\\s*([A-Za-z0-9_]+)"
                        + ".*?(?:长度|权重|距离|代价|花费|成本)\\s*(?:为|是|=|改成|改为|变成|设为|调整为)?\\s*(\\d+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return new String[]{matcher.group(1), matcher.group(2), matcher.group(3)};
        }
        return null;
    }
}