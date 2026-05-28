package Test;

class AgentContext {
    private Intent lastIntent;
    private String lastSource;
    private String lastTarget;
    private PathOptimizerAgent.PathResult lastPathResult;
    private boolean lastSuccess;

    void rememberCommand(Intent intent, boolean success) {
        this.lastIntent = intent;
        this.lastSuccess = success;
    }

    void rememberPath(String source, String target,
                      PathOptimizerAgent.PathResult pathResult) {
        this.lastSource = source;
        this.lastTarget = target;
        this.lastPathResult = pathResult;
    }

    boolean hasLastPath() {
        return lastPathResult != null;
    }

    Intent getLastIntent() {
        return lastIntent;
    }

    String getLastOriginalInput() {
        return lastIntent != null ? lastIntent.getRawInput() : null;
    }

    String getLastNormalizedCommand() {
        return lastIntent != null ? lastIntent.toNormalizedCommand() : null;
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