package Test;

class Edge {
    String target;
    int weight;
    LinkMetrics metrics; // 可为 null，表示无多维指标

    Edge(String target, int weight) {
        this(target, weight, null);
    }

    Edge(String target, int weight, LinkMetrics metrics) {
        this.target = target;
        this.weight = weight;
        this.metrics = metrics;
    }

    /**
     * 根据优化策略获取代价
     * WEIGHT 或 metrics 为 null 时返回 weight（兼容旧逻辑）
     */
    int getCost(OptimizeStrategy strategy) {
        if (strategy == OptimizeStrategy.WEIGHT || metrics == null) {
            return weight;
        }
        return metrics.getCost(strategy);
    }

    @Override
    public String toString() {
        if (metrics != null) {
            return target + "(" + weight + "|" + metrics.toShortString() + ")";
        }
        return target + "(" + weight + ")";
    }
}
