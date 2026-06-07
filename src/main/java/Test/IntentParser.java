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

        intent = parseKPath(text, input);
        if (intent != null) return intent;

        intent = parseConstrain(text, input);
        if (intent != null) return intent;

        intent = parseStrategy(text, input);
        if (intent != null) return intent;

        intent = parseShortestPath(text, input);
        if (intent != null) return intent;

        intent = parseReachable(text, input);
        if (intent != null) return intent;

        intent = parseExplain(text, input);
        if (intent != null) return intent;

        intent = parseTopologySummary(text, input);
        if (intent != null) return intent;

        intent = parseAnalyze(text, input);
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
                if (parts.length >= 4) {
                    try {
                        int weight = Integer.parseInt(parts[3]);
                        LinkMetrics metrics = parseMetricsFromParts(parts, 4);
                        return Intent.addDirected(text, parts[1], parts[2], weight, metrics);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                break;
            case "addud":
                if (parts.length >= 4) {
                    try {
                        int weight = Integer.parseInt(parts[3]);
                        LinkMetrics metrics = parseMetricsFromParts(parts, 4);
                        return Intent.addUndirected(text, parts[1], parts[2], weight, metrics);
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
            case "kpath":
                if (parts.length == 4) {
                    try {
                        return Intent.kPath(text, parts[1], parts[2], Integer.parseInt(parts[3]));
                    } catch (NumberFormatException e) {
                        return null;
                    }
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
            case "analyze":
                return Intent.analyze(text);
            case "constrain":
                return parseConstrainCommand(parts, text);
            case "strategy":
                return parseStrategyCommand(parts, text);
            default:
                return null;
        }
        return null;
    }

    // ==================== 自然语言解析 ====================

    private Intent parseConstrainCommand(String[] parts, String raw) {
        // constrain path <src> <dst> [via <n1,n2>] [avoid <n3>] [hops <N>]
        if (parts.length < 4) return null;
        // parts[0]=constrain, parts[1]=path, parts[2]=src, parts[3]=dst
        if (!parts[1].equalsIgnoreCase("path")) return null;
        String src = parts[2];
        String dst = parts[3];

        java.util.List<String> viaNodes = new java.util.ArrayList<>();
        java.util.List<String> avoidNodes = new java.util.ArrayList<>();
        Integer maxHops = null;

        int i = 4;
        while (i < parts.length) {
            String keyword = parts[i].toLowerCase();
            if (keyword.equals("via") && i + 1 < parts.length) {
                for (String node : parts[i + 1].split(",")) {
                    String n = node.trim();
                    if (!n.isEmpty()) viaNodes.add(n);
                }
                i += 2;
            } else if (keyword.equals("avoid") && i + 1 < parts.length) {
                for (String node : parts[i + 1].split(",")) {
                    String n = node.trim();
                    if (!n.isEmpty()) avoidNodes.add(n);
                }
                i += 2;
            } else if (keyword.equals("hops") && i + 1 < parts.length) {
                try {
                    maxHops = Integer.parseInt(parts[i + 1]);
                } catch (NumberFormatException e) {
                    return null;
                }
                i += 2;
            } else {
                return null;
            }
        }

        if (viaNodes.size() > 3) {
            // 超出限制，返回 null 让上层走 UNKNOWN 报错
            return null;
        }

        return Intent.constrain(raw, src, dst, viaNodes, avoidNodes, maxHops);
    }

    private Intent parseStrategyCommand(String[] parts, String raw) {
        if (parts.length < 2) return null;
        OptimizeStrategy strategy = parseStrategyName(parts[1]);
        if (strategy == null) return null;
        return Intent.strategy(raw, strategy);
    }

    private OptimizeStrategy parseStrategyName(String name) {
        switch (name.toLowerCase()) {
            case "weight":
            case "权重":     return OptimizeStrategy.WEIGHT;
            case "delay":
            case "时延":
            case "延迟":     return OptimizeStrategy.DELAY;
            case "bandwidth":
            case "带宽":     return OptimizeStrategy.BANDWIDTH;
            case "loss":
            case "丢包":
            case "丢包率":   return OptimizeStrategy.PACKET_LOSS;
            case "reliability":
            case "可靠":
            case "可靠性":   return OptimizeStrategy.RELIABILITY;
            default:         return null;
        }
    }

    /**
     * 从 add 命令的 parts 数组中解析可选的 LinkMetrics
     * 格式: add A B 5 delay 10 bandwidth 100 loss 0.5 reliability 99.9
     * 从 startIndex 开始，支持 key-value 对（4个可选指标，顺序不固定）
     */
    private LinkMetrics parseMetricsFromParts(String[] parts, int startIndex) {
        if (startIndex >= parts.length) return null;

        Integer delay = null;
        Integer bandwidth = null;
        Double packetLoss = null;
        Double reliability = null;

        int i = startIndex;
        while (i < parts.length - 1) {
            String key = parts[i].toLowerCase();
            String val = parts[i + 1];
            try {
                switch (key) {
                    case "delay":
                    case "时延":
                        delay = Integer.parseInt(val);
                        break;
                    case "bandwidth":
                    case "带宽":
                        bandwidth = Integer.parseInt(val);
                        break;
                    case "loss":
                    case "丢包":
                    case "丢包率":
                        packetLoss = Double.parseDouble(val);
                        break;
                    case "reliability":
                    case "可靠":
                    case "可靠性":
                        reliability = Double.parseDouble(val);
                        break;
                    default:
                        return null; // 未知 key，忽略 metrics
                }
            } catch (NumberFormatException e) {
                return null;
            }
            i += 2;
        }

        if (delay == null || bandwidth == null || packetLoss == null || reliability == null) {
            return null; // 四个指标必须全部提供
        }
        return new LinkMetrics(delay, bandwidth, packetLoss, reliability);
    }

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

    private Intent parseKPath(String text, String raw) {
        if (!(text.contains("条最短路径") || text.contains("条路径")
                || text.contains("备选路径") || text.contains("前几条")
                || text.contains("条短路径"))) {
            return null;
        }
        String[] nodes = extractTwoNodes(text);
        if (nodes == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*条").matcher(text);
        if (!m.find()) return null;
        int k = Integer.parseInt(m.group(1));
        if (k <= 0 || k > 10) return null;
        return Intent.kPath(raw, nodes[0], nodes[1], k);
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
        // 如果包含分析意图的关键词，优先留给 parseAnalyze
        if (text.contains("关键") || text.contains("瓶颈") || text.contains("影响")) {
            return null;
        }
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

    private Intent parseAnalyze(String text, String raw) {
        if (text.contains("关键节点") || text.contains("瓶颈") || text.contains("关键链路")
                || text.contains("影响分析") || text.contains("拓扑分析")) {
            return Intent.analyze(raw);
        }
        if (text.contains("分析") && (text.contains("关键") || text.contains("瓶颈")
                || text.contains("影响") || text.contains("节点") || text.contains("链路"))) {
            return Intent.analyze(raw);
        }
        return null;
    }

    private Intent parseStrategy(String text, String raw) {
        if (!(text.contains("策略") || text.contains("优化目标") || text.contains("优化方式")
                || text.contains("以时延") || text.contains("以带宽") || text.contains("以丢包")
                || text.contains("以可靠") || text.contains("用时延") || text.contains("用带宽")
                || text.contains("用丢包") || text.contains("用可靠"))) {
            return null;
        }

        OptimizeStrategy strategy = null;
        if (text.contains("时延") || text.contains("延迟")) strategy = OptimizeStrategy.DELAY;
        else if (text.contains("带宽")) strategy = OptimizeStrategy.BANDWIDTH;
        else if (text.contains("丢包")) strategy = OptimizeStrategy.PACKET_LOSS;
        else if (text.contains("可靠")) strategy = OptimizeStrategy.RELIABILITY;
        else if (text.contains("权重") || text.contains("综合")) strategy = OptimizeStrategy.WEIGHT;

        if (strategy == null) return null;
        return Intent.strategy(raw, strategy);
    }

    private Intent parseConstrain(String text, String raw) {
        if (!(text.contains("约束") || text.contains("限制") || text.contains("必经")
                || text.contains("避开") || text.contains("跳数") || text.contains("经过"))) {
            return null;
        }
        String[] nodes = extractTwoNodes(text);
        if (nodes == null) return null;

        java.util.List<String> viaNodes = new java.util.ArrayList<>();
        java.util.List<String> avoidNodes = new java.util.ArrayList<>();
        Integer maxHops = null;

        // 提取必经节点：经过X、必经X
        java.util.regex.Pattern viaPattern = java.util.regex.Pattern.compile(
                "(?:经过|必经|途经)\\s*([A-Za-z0-9_]+(?:\\s*[、,和与]\\s*[A-Za-z0-9_]+)*)");
        java.util.regex.Matcher viaMatcher = viaPattern.matcher(text);
        if (viaMatcher.find()) {
            String[] viaParts = viaMatcher.group(1).split("[、,和与\\s]+");
            for (String v : viaParts) {
                String n = v.trim();
                if (!n.isEmpty()) viaNodes.add(n);
            }
        }

        // 提取避开节点：避开X、不经过X
        java.util.regex.Pattern avoidPattern = java.util.regex.Pattern.compile(
                "(?:避开|不经过|不走|绕开|排除)\\s*([A-Za-z0-9_]+(?:\\s*[、,和与]\\s*[A-Za-z0-9_]+)*)");
        java.util.regex.Matcher avoidMatcher = avoidPattern.matcher(text);
        if (avoidMatcher.find()) {
            String[] avoidParts = avoidMatcher.group(1).split("[、,和与\\s]+");
            for (String v : avoidParts) {
                String n = v.trim();
                if (!n.isEmpty()) avoidNodes.add(n);
            }
        }

        // 提取跳数限制：跳数不超过N、最多N跳、最多经过N个
        java.util.regex.Pattern hopsPattern = java.util.regex.Pattern.compile(
                "(?:跳数|跳|经过|中转)\\s*(?:不超过|最多|不大于|小于)?\\s*(\\d+)");
        java.util.regex.Matcher hopsMatcher = hopsPattern.matcher(text);
        if (hopsMatcher.find()) {
            maxHops = Integer.parseInt(hopsMatcher.group(1));
        }

        if (viaNodes.isEmpty() && avoidNodes.isEmpty() && maxHops == null) {
            return null;
        }
        if (viaNodes.size() > 3) return null;

        return Intent.constrain(raw, nodes[0], nodes[1], viaNodes, avoidNodes, maxHops);
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