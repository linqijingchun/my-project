package Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class IntentParser {
    String parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        String text = input.trim();

        if (isCommand(text)) {
            return text;
        }

        String command;

        command = parseAddDirectedEdge(text);
        if (command != null) return command;

        command = parseAddUndirectedEdge(text);
        if (command != null) return command;

        command = parseUpdateWeight(text);
        if (command != null) return command;

        command = parseRemoveEdge(text);
        if (command != null) return command;

        command = parseAllShortestPaths(text);
        if (command != null) return command;

        command = parseShortestPath(text);
        if (command != null) return command;

        command = parseReachable(text);
        if (command != null) return command;

        command = parseExplain(text);
        if (command != null) return command;

        command = parseShowGraph(text);
        if (command != null) return command;

        return text;
    }

    private boolean isCommand(String text) {
        String first = text.split("\\s+")[0].toLowerCase();
        return first.equals("add")
                || first.equals("addud")
                || first.equals("path")
                || first.equals("reach")
                || first.equals("update")
                || first.equals("remove")
                || first.equals("show")
                || first.equals("load")
                || first.equals("save")
                || first.equals("allpaths")
                || first.equals("explain")
                || first.equals("help");
    }

    private String parseAddDirectedEdge(String text) {
        if (!(text.contains("添加") || text.contains("新增") || text.contains("加入"))) {
            return null;
        }

        if (text.contains("无向")) {
            return null;
        }

        String[] edge = extractEdgeWithWeight(text);
        if (edge == null) {
            return null;
        }

        return "add " + edge[0] + " " + edge[1] + " " + edge[2];
    }

    private String parseAddUndirectedEdge(String text) {
        if (!(text.contains("无向") || text.contains("双向") || text.contains("互通"))) {
            return null;
        }

        String[] edge = extractEdgeWithWeight(text);
        if (edge == null) {
            return null;
        }

        return "addud " + edge[0] + " " + edge[1] + " " + edge[2];
    }

    private String parseUpdateWeight(String text) {
        if (!(text.contains("修改")
                || text.contains("更新")
                || text.contains("改成")
                || text.contains("改为")
                || text.contains("变成")
                || text.contains("设为")
                || text.contains("设置")
                || text.contains("调整")
                || text.contains("将"))) {
            return null;
        }

        String[] edge = extractEdgeWithWeight(text);
        if (edge == null) {
            return null;
        }

        return "update " + edge[0] + " " + edge[1] + " " + edge[2];
    }

    private String parseRemoveEdge(String text) {
        if (!(text.contains("删除") || text.contains("移除") || text.contains("去掉"))) {
            return null;
        }

        String[] nodes = extractTwoNodes(text);
        if (nodes == null) {
            return null;
        }

        return "remove " + nodes[0] + " " + nodes[1];
    }

    private String parseAllShortestPaths(String text) {
        if (!(text.contains("所有最短路径")
                || text.contains("全部最短路径")
                || text.contains("所有路径")
                || text.contains("全部路径"))) {
            return null;
        }

        String[] nodes = extractTwoNodes(text);
        if (nodes == null) {
            return null;
        }

        return "allpaths " + nodes[0] + " " + nodes[1];
    }

    private String parseShortestPath(String text) {
        if (!(text.contains("最短路径")
                || text.contains("最优路径")
                || text.contains("怎么走")
                || text.contains("最便宜")
                || text.contains("总代价最小"))) {
            return null;
        }

        String[] nodes = extractTwoNodes(text);
        if (nodes == null) {
            return null;
        }

        return "path " + nodes[0] + " " + nodes[1];
    }

    private String parseReachable(String text) {
        if (!(text.contains("可达")
                || text.contains("能到")
                || text.contains("能不能到")
                || text.contains("是否能到")
                || text.contains("能否到达"))) {
            return null;
        }

        String[] nodes = extractTwoNodes(text);
        if (nodes == null) {
            return null;
        }

        return "reach " + nodes[0] + " " + nodes[1];
    }

    private String parseShowGraph(String text) {
        if (text.contains("显示") || text.contains("查看") || text.contains("展示")) {
            if (text.contains("图") || text.contains("拓扑") || text.contains("网络")) {
                return "show";
            }
        }

        return null;
    }

    private String[] extractTwoNodes(String text) {
        Pattern pattern = Pattern.compile("([A-Za-z0-9_]+)\\s*(?:到|->|至|和|与)\\s*([A-Za-z0-9_]+)");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return new String[]{matcher.group(1), matcher.group(2)};
        }

        return null;
    }

    private String[] extractEdgeWithWeight(String text) {
        Pattern pattern = Pattern.compile(
                "([A-Za-z0-9_]+)\\s*(?:到|->|至|和|与)\\s*([A-Za-z0-9_]+).*?(?:长度|权重|距离|代价|花费|成本)\\s*(?:为|是|=|改成|改为|变成|设为|调整为)?\\s*(\\d+)"
        );

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return new String[]{
                    matcher.group(1),
                    matcher.group(2),
                    matcher.group(3)
            };
        }

        return null;
    }

    private String parseExplain(String text) {
        if (text.equalsIgnoreCase("why")
                || text.contains("为什么")
                || text.contains("为啥")
                || text.contains("解释")
                || text.contains("原因")) {
            return "explain";
        }

        return null;
    }
}