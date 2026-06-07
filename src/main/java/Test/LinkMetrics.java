package Test;

class LinkMetrics {
    static final int MAX_BANDWIDTH = 100000; // Mbps，用于带宽方向转换

    final int delay;          // 时延 ms，正整数
    final int bandwidth;      // 带宽 Mbps，正整数
    final double packetLoss;  // 丢包率 %，0.0~100.0
    final double reliability; // 可靠性 %，0.0~100.0

    LinkMetrics(int delay, int bandwidth, double packetLoss, double reliability) {
        if (delay <= 0) throw new IllegalArgumentException("时延必须是正整数");
        if (bandwidth <= 0) throw new IllegalArgumentException("带宽必须是正整数");
        if (packetLoss < 0 || packetLoss > 100) throw new IllegalArgumentException("丢包率必须在 0~100 之间");
        if (reliability < 0 || reliability > 100) throw new IllegalArgumentException("可靠性必须在 0~100 之间");
        this.delay = delay;
        this.bandwidth = bandwidth;
        this.packetLoss = packetLoss;
        this.reliability = reliability;
    }

    /**
     * 根据优化策略返回该链路的代价值（int，越小越好）
     */
    int getCost(OptimizeStrategy strategy) {
        switch (strategy) {
            case DELAY:      return delay;
            case BANDWIDTH:  return MAX_BANDWIDTH - bandwidth;
            case PACKET_LOSS: return (int) (packetLoss * 100);       // 0.5% → 50
            case RELIABILITY: return (int) ((100.0 - reliability) * 100); // 99.5% → 50
            default:         return 0;
        }
    }

    String toShortString() {
        return "d=" + delay + " b=" + bandwidth
                + " l=" + packetLoss + " r=" + reliability;
    }

    /**
     * 输出为文件存储格式（6列中的后4列）
     */
    String toFileString() {
        return delay + " " + bandwidth + " " + packetLoss + " " + reliability;
    }

    /**
     * 从文件列解析（传入后4列的字符串数组）
     */
    static LinkMetrics fromFileString(String[] cols) {
        return new LinkMetrics(
                Integer.parseInt(cols[0]),
                Integer.parseInt(cols[1]),
                Double.parseDouble(cols[2]),
                Double.parseDouble(cols[3])
        );
    }
}
