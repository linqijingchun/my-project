package Test;

import java.util.Collections;
import java.util.List;

class AgentResponse {
    private final boolean success;
    private final String message;
    private final PathOptimizerAgent.PathResult pathResult;
    private final List<PathOptimizerAgent.PathResult> pathResults;
    private final String originalInput;
    private final String normalizedCommand;

    private AgentResponse(boolean success, String message,
                          PathOptimizerAgent.PathResult pathResult,
                          List<PathOptimizerAgent.PathResult> pathResults,
                          String originalInput, String normalizedCommand) {
        this.success = success;
        this.message = message;
        this.pathResult = pathResult;
        this.pathResults = pathResults;
        this.originalInput = originalInput;
        this.normalizedCommand = normalizedCommand;
    }

    static AgentResponse success(String message) {
        return new AgentResponse(true, message, null,
                Collections.emptyList(), null, null);
    }

    static AgentResponse successWithPath(String message,
                                         PathOptimizerAgent.PathResult pathResult) {
        return new AgentResponse(true, message, pathResult,
                Collections.emptyList(), null, null);
    }

    static AgentResponse successWithPaths(String message,
                                          List<PathOptimizerAgent.PathResult> pathResults) {
        return new AgentResponse(true, message, null,
                pathResults, null, null);
    }

    static AgentResponse error(String message) {
        return new AgentResponse(false, message, null,
                Collections.emptyList(), null, null);
    }

    // 替代原来的 withCommandInfo(String, String)
    AgentResponse withIntent(Intent intent) {
        return new AgentResponse(
                this.success, this.message, this.pathResult, this.pathResults,
                intent.getRawInput(), intent.toNormalizedCommand());
    }

    public boolean isSuccess()       { return success; }
    public String getMessage()       { return message; }
    public PathOptimizerAgent.PathResult getPathResult() { return pathResult; }
    public List<PathOptimizerAgent.PathResult> getPathResults() { return pathResults; }
    public String getOriginalInput()     { return originalInput; }
    public String getNormalizedCommand() { return normalizedCommand; }
}