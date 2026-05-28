package Test;

class Intent {
    private final IntentType type;
    private final String source;
    private final String target;
    private final Integer weight;
    private final String filename;
    private final String rawInput;

    private Intent(IntentType type, String source, String target,
                   Integer weight, String filename, String rawInput) {
        this.type = type;
        this.source = source;
        this.target = target;
        this.weight = weight;
        this.filename = filename;
        this.rawInput = rawInput;
    }

    // ---------- 工厂方法：每种意图一个，语义清晰 ----------

    static Intent addDirected(String rawInput, String from, String to, int weight) {
        return new Intent(IntentType.ADD_DIRECTED, from, to, weight, null, rawInput);
    }

    static Intent addUndirected(String rawInput, String from, String to, int weight) {
        return new Intent(IntentType.ADD_UNDIRECTED, from, to, weight, null, rawInput);
    }

    static Intent path(String rawInput, String src, String dst) {
        return new Intent(IntentType.PATH, src, dst, null, null, rawInput);
    }

    static Intent allPaths(String rawInput, String src, String dst) {
        return new Intent(IntentType.ALLPATHS, src, dst, null, null, rawInput);
    }

    static Intent reach(String rawInput, String src, String dst) {
        return new Intent(IntentType.REACH, src, dst, null, null, rawInput);
    }

    static Intent update(String rawInput, String from, String to, int weight) {
        return new Intent(IntentType.UPDATE, from, to, weight, null, rawInput);
    }

    static Intent remove(String rawInput, String from, String to) {
        return new Intent(IntentType.REMOVE, from, to, null, null, rawInput);
    }

    static Intent show(String rawInput) {
        return new Intent(IntentType.SHOW, null, null, null, null, rawInput);
    }

    static Intent load(String rawInput, String filename) {
        return new Intent(IntentType.LOAD, null, null, null, filename, rawInput);
    }

    static Intent save(String rawInput, String filename) {
        return new Intent(IntentType.SAVE, null, null, null, filename, rawInput);
    }

    static Intent explain(String rawInput) {
        return new Intent(IntentType.EXPLAIN, null, null, null, null, rawInput);
    }

    static Intent help(String rawInput) {
        return new Intent(IntentType.HELP, null, null, null, null, rawInput);
    }

    static Intent summary(String rawInput) {
        return new Intent(IntentType.SUMMARY, null, null, null, null, rawInput);
    }

    static Intent unknown(String rawInput) {
        return new Intent(IntentType.UNKNOWN, null, null, null, null, rawInput);
    }

    // ---------- Getters ----------

    IntentType getType()     { return type; }
    String getSource()       { return source; }
    String getTarget()       { return target; }
    Integer getWeight()      { return weight; }
    String getFilename()     { return filename; }
    String getRawInput()     { return rawInput; }
    boolean isUnknown()      { return type == IntentType.UNKNOWN; }

    // ---------- 派生标准命令字符串（给上下文记忆和显示用） ----------

    String toNormalizedCommand() {
        switch (type) {
            case ADD_DIRECTED:   return "add " + source + " " + target + " " + weight;
            case ADD_UNDIRECTED: return "addud " + source + " " + target + " " + weight;
            case PATH:           return "path " + source + " " + target;
            case ALLPATHS:       return "allpaths " + source + " " + target;
            case REACH:          return "reach " + source + " " + target;
            case UPDATE:         return "update " + source + " " + target + " " + weight;
            case REMOVE:         return "remove " + source + " " + target;
            case SHOW:           return "show";
            case LOAD:           return "load " + filename;
            case SAVE:           return "save " + filename;
            case EXPLAIN:        return "explain";
            case HELP:           return "help";
            case SUMMARY:        return "summary";
            default:             return rawInput;
        }
    }
}