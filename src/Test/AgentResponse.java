package Test;

import java.util.Collections;
import java.util.List;

class AgentResponse {
    private final boolean success;
    private final String message;
    private final PathOptimizerAgent.PathResult pathResult;
    private final List<PathOptimizerAgent.PathResult> pathResults;

    private AgentResponse(boolean success,
                          String message,
                          PathOptimizerAgent.PathResult pathResult,
                          List<PathOptimizerAgent.PathResult> pathResults) {
        this.success = success;
        this.message = message;
        this.pathResult = pathResult;
        this.pathResults = pathResults;
    }

    public static AgentResponse success(String message) {
        return new AgentResponse(true, message, null, Collections.emptyList());
    }

    public static AgentResponse successWithPath(String message, PathOptimizerAgent.PathResult pathResult) {
        return new AgentResponse(true, message, pathResult, Collections.emptyList());
    }

    public static AgentResponse successWithPaths(String message, List<PathOptimizerAgent.PathResult> pathResults) {
        return new AgentResponse(true, message, null, pathResults);
    }

    public static AgentResponse error(String message) {
        return new AgentResponse(false, message, null, Collections.emptyList());
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public PathOptimizerAgent.PathResult getPathResult() {
        return pathResult;
    }

    public List<PathOptimizerAgent.PathResult> getPathResults() {
        return pathResults;
    }
}