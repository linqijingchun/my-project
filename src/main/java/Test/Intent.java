package Test;

import java.util.Collections;
import java.util.List;

class Intent {
    private final IntentType type;
    private final String source;
    private final String target;
    private final Integer weight;
    private final String filename;
    private final String rawInput;
    private final List<String> viaNodes;
    private final List<String> avoidNodes;
    private final Integer maxHops;
    private final OptimizeStrategy strategy;
    private final LinkMetrics metrics;
    private final Integer k;

    private Intent(IntentType type, String source, String target,
                   Integer weight, String filename, String rawInput,
                   List<String> viaNodes, List<String> avoidNodes, Integer maxHops,
                   OptimizeStrategy strategy, LinkMetrics metrics, Integer k) {
        this.type = type;
        this.source = source;
        this.target = target;
        this.weight = weight;
        this.filename = filename;
        this.rawInput = rawInput;
        this.viaNodes = viaNodes != null ? viaNodes : Collections.emptyList();
        this.avoidNodes = avoidNodes != null ? avoidNodes : Collections.emptyList();
        this.maxHops = maxHops;
        this.strategy = strategy;
        this.metrics = metrics;
        this.k = k;
    }

    // ---------- 工厂方法 ----------

    static Intent addDirected(String rawInput, String from, String to, int weight) {
        return new Intent(IntentType.ADD_DIRECTED, from, to, weight, null, rawInput, null, null, null, null, null, null);
    }

    static Intent addDirected(String rawInput, String from, String to, int weight, LinkMetrics metrics) {
        return new Intent(IntentType.ADD_DIRECTED, from, to, weight, null, rawInput, null, null, null, null, metrics, null);
    }

    static Intent addUndirected(String rawInput, String from, String to, int weight) {
        return new Intent(IntentType.ADD_UNDIRECTED, from, to, weight, null, rawInput, null, null, null, null, null, null);
    }

    static Intent addUndirected(String rawInput, String from, String to, int weight, LinkMetrics metrics) {
        return new Intent(IntentType.ADD_UNDIRECTED, from, to, weight, null, rawInput, null, null, null, null, metrics, null);
    }

    static Intent path(String rawInput, String src, String dst) {
        return new Intent(IntentType.PATH, src, dst, null, null, rawInput, null, null, null, null, null, null);
    }

    static Intent allPaths(String rawInput, String src, String dst) {
        return new Intent(IntentType.ALLPATHS, src, dst, null, null, rawInput, null, null, null, null, null, null);
    }

    static Intent kPath(String rawInput, String src, String dst, int k) {
        return new Intent(IntentType.KPATH, src, dst, null, null, rawInput, null, null, null, null, null, k);
    }

    static Intent reach(String rawInput, String src, String dst) {
        return new Intent(IntentType.REACH, src, dst, null, null, rawInput, null, null, null, null, null, null);
    }

    static Intent update(String rawInput, String from, String to, int weight) {
        return new Intent(IntentType.UPDATE, from, to, weight, null, rawInput, null, null, null, null, null, null);
    }

    static Intent remove(String rawInput, String from, String to) {
        return new Intent(IntentType.REMOVE, from, to, null, null, rawInput, null, null, null, null, null, null);
    }

    static Intent show(String rawInput) {
        return new Intent(IntentType.SHOW, null, null, null, null, rawInput, null, null, null, null, null, null);
    }

    static Intent load(String rawInput, String filename) {
        return new Intent(IntentType.LOAD, null, null, null, filename, rawInput, null, null, null, null, null, null);
    }

    static Intent save(String rawInput, String filename) {
        return new Intent(IntentType.SAVE, null, null, null, filename, rawInput, null, null, null, null, null, null);
    }

    static Intent explain(String rawInput) {
        return new Intent(IntentType.EXPLAIN, null, null, null, null, rawInput, null, null, null, null, null, null);
    }

    static Intent help(String rawInput) {
        return new Intent(IntentType.HELP, null, null, null, null, rawInput, null, null, null, null, null, null);
    }

    static Intent summary(String rawInput) {
        return new Intent(IntentType.SUMMARY, null, null, null, null, rawInput, null, null, null, null, null, null);
    }

    static Intent analyze(String rawInput) {
        return new Intent(IntentType.ANALYZE, null, null, null, null, rawInput, null, null, null, null, null, null);
    }

    static Intent constrain(String rawInput, String src, String dst,
                            List<String> viaNodes, List<String> avoidNodes, Integer maxHops) {
        return new Intent(IntentType.CONSTRAIN, src, dst, null, null, rawInput, viaNodes, avoidNodes, maxHops, null, null, null);
    }

    static Intent strategy(String rawInput, OptimizeStrategy strategy) {
        return new Intent(IntentType.STRATEGY, null, null, null, null, rawInput, null, null, null, strategy, null, null);
    }

    static Intent unknown(String rawInput) {
        return new Intent(IntentType.UNKNOWN, null, null, null, null, rawInput, null, null, null, null, null, null);
    }

    // ---------- Getters ----------

    IntentType getType()     { return type; }
    String getSource()       { return source; }
    String getTarget()       { return target; }
    Integer getWeight()      { return weight; }
    String getFilename()     { return filename; }
    String getRawInput()     { return rawInput; }
    List<String> getViaNodes()   { return viaNodes; }
    List<String> getAvoidNodes() { return avoidNodes; }
    Integer getMaxHops()         { return maxHops; }
    OptimizeStrategy getStrategy() { return strategy; }
    LinkMetrics getMetrics()     { return metrics; }
    Integer getK()             { return k; }
    boolean isUnknown()      { return type == IntentType.UNKNOWN; }

    // ---------- 派生标准命令字符串 ----------

    String toNormalizedCommand() {
        switch (type) {
            case ADD_DIRECTED:   return buildAddCommand("add");
            case ADD_UNDIRECTED: return buildAddCommand("addud");
            case PATH:           return "path " + source + " " + target;
            case ALLPATHS:       return "allpaths " + source + " " + target;
            case KPATH:          return "kpath " + source + " " + target + " " + k;
            case REACH:          return "reach " + source + " " + target;
            case UPDATE:         return "update " + source + " " + target + " " + weight;
            case REMOVE:         return "remove " + source + " " + target;
            case SHOW:           return "show";
            case LOAD:           return "load " + filename;
            case SAVE:           return "save " + filename;
            case EXPLAIN:        return "explain";
            case HELP:           return "help";
            case SUMMARY:        return "summary";
            case ANALYZE:        return "analyze";
            case CONSTRAIN:      return buildConstrainCommand();
            case STRATEGY:       return "strategy " + strategyName();
            default:             return rawInput;
        }
    }

    private String buildAddCommand(String cmd) {
        StringBuilder sb = new StringBuilder(cmd + " " + source + " " + target + " " + weight);
        if (metrics != null) {
            sb.append(" ").append(metrics.toFileString());
        }
        return sb.toString();
    }

    private String buildConstrainCommand() {
        StringBuilder sb = new StringBuilder("constrain path " + source + " " + target);
        if (!viaNodes.isEmpty()) {
            sb.append(" via ").append(String.join(",", viaNodes));
        }
        if (!avoidNodes.isEmpty()) {
            sb.append(" avoid ").append(String.join(",", avoidNodes));
        }
        if (maxHops != null) {
            sb.append(" hops ").append(maxHops);
        }
        return sb.toString();
    }

    private String strategyName() {
        if (strategy == null) return "weight";
        switch (strategy) {
            case DELAY:      return "delay";
            case BANDWIDTH:  return "bandwidth";
            case PACKET_LOSS: return "loss";
            case RELIABILITY: return "reliability";
            default:         return "weight";
        }
    }
}
