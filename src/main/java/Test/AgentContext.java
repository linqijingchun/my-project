package Test;

class AgentContext {
    private String lastOriginalInput;
    private String lastNormalizedCommand;
    private String lastSource;
    private String lastTarget;
    private PathOptimizerAgent.PathResult lastPathResult;
    private boolean lastSuccess;

    void rememberCommand(String originalInput, String normalizedCommand, boolean success) {
        this.lastOriginalInput = originalInput;
        this.lastNormalizedCommand = normalizedCommand;
        this.lastSuccess = success;
    }

    void rememberPath(String source, String target, PathOptimizerAgent.PathResult pathResult) {
        this.lastSource = source;
        this.lastTarget = target;
        this.lastPathResult = pathResult;
    }

    boolean hasLastPath() {
        return lastPathResult != null;
    }

    String getLastOriginalInput() {
        return lastOriginalInput;
    }

    String getLastNormalizedCommand() {
        return lastNormalizedCommand;
    }

    String getLastSource() {
        return lastSource;
    }

    String getLastTarget() {
        return lastTarget;
    }

    PathOptimizerAgent.PathResult getLastPathResult() {
        return lastPathResult;
    }

    boolean isLastSuccess() {
        return lastSuccess;
    }
}
