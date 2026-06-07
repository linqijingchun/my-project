package Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import Test.PathOptimizerAgent.PathResult;
import Test.PathOptimizerAgent.TopologyAnalysisResult;

/**
 * 网络路径寻优智能体 - 图形界面版
 */
public class NetworkPathGUI extends JFrame {
    private PathOptimizerAgent agent;
    private AgentCommandService commandService;
    private GraphPanel graphPanel;
    private JTextArea messageArea;
    private JTextField srcField, dstField, fromField, toField, weightField, fileField;
    private JTextField kField;
    private JTextField viaField, avoidField, hopsField;
    private JTextField nlpField;
    private JComboBox<String> strategyBox;
    private PathResult currentHighlightPath;

    public NetworkPathGUI() {
        agent = new PathOptimizerAgent();
        commandService = new AgentCommandService(agent);
        initDemo();
        initUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initDemo() {
        commandService.handle("add A B 5 delay 10 bandwidth 100 loss 0.5 reliability 99.9");
        commandService.handle("add A C 2 delay 30 bandwidth 200 loss 0.1 reliability 99.99");
        commandService.handle("add B D 1 delay 5 bandwidth 100 loss 0.2 reliability 99.95");
        commandService.handle("add C B 3 delay 15 bandwidth 150 loss 0.3 reliability 99.9");
        commandService.handle("add C D 4 delay 20 bandwidth 80 loss 1.0 reliability 99.0");
        commandService.handle("add D E 2 delay 8 bandwidth 300 loss 0.05 reliability 99.99");
    }

    private void initUI() {
        setTitle("网络路径寻优智能体");
        setLayout(new BorderLayout());

        graphPanel = new GraphPanel(agent, this);
        add(graphPanel, BorderLayout.CENTER);

        // 右侧控制面板（全部放在滚动面板中）
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("操作面板"));

        // === 添加边 ===
        JPanel addEdgePanel = new JPanel(new GridLayout(4, 2, 5, 5));
        addEdgePanel.setBorder(BorderFactory.createTitledBorder("添加边"));
        addEdgePanel.add(new JLabel("起点:"));
        fromField = new JTextField(8);
        addEdgePanel.add(fromField);
        addEdgePanel.add(new JLabel("终点:"));
        toField = new JTextField(8);
        addEdgePanel.add(toField);
        addEdgePanel.add(new JLabel("权重:"));
        weightField = new JTextField(8);
        addEdgePanel.add(weightField);
        JButton addDirectedBtn = new JButton("添加有向边");
        JButton addUndirectedBtn = new JButton("添加无向边");
        addEdgePanel.add(addDirectedBtn);
        addEdgePanel.add(addUndirectedBtn);
        controlPanel.add(addEdgePanel);

        // === 路径查询 ===
        JPanel queryPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        queryPanel.setBorder(BorderFactory.createTitledBorder("路径查询"));
        queryPanel.add(new JLabel("起点:"));
        srcField = new JTextField(8);
        queryPanel.add(srcField);
        queryPanel.add(new JLabel("终点:"));
        dstField = new JTextField(8);
        queryPanel.add(dstField);
        queryPanel.add(new JLabel("K值:"));
        kField = new JTextField("3", 8);
        queryPanel.add(kField);
        JButton pathBtn = new JButton("最短路径");
        JButton allPathsBtn = new JButton("所有最短路径");
        JButton kPathBtn = new JButton("K条路径");
        JButton reachBtn = new JButton("可达判断");
        queryPanel.add(pathBtn);
        queryPanel.add(allPathsBtn);
        queryPanel.add(kPathBtn);
        queryPanel.add(reachBtn);
        controlPanel.add(queryPanel);

        // === 约束路径 ===
        JPanel constrainPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        constrainPanel.setBorder(BorderFactory.createTitledBorder("约束路径"));
        constrainPanel.add(new JLabel("必经节点(via):"));
        viaField = new JTextField(8);
        constrainPanel.add(viaField);
        constrainPanel.add(new JLabel("避开节点(avoid):"));
        avoidField = new JTextField(8);
        constrainPanel.add(avoidField);
        constrainPanel.add(new JLabel("最大跳数(hops):"));
        hopsField = new JTextField(8);
        constrainPanel.add(hopsField);
        JButton constrainBtn = new JButton("约束查询");
        JButton clearConstrainBtn = new JButton("清空");
        constrainPanel.add(constrainBtn);
        constrainPanel.add(clearConstrainBtn);
        controlPanel.add(constrainPanel);

        // === 拓扑文件 ===
        JPanel filePanel = new JPanel(new GridLayout(2, 2, 5, 5));
        filePanel.setBorder(BorderFactory.createTitledBorder("拓扑文件"));
        filePanel.add(new JLabel("文件名:"));
        fileField = new JTextField(8);
        filePanel.add(fileField);
        JButton loadBtn = new JButton("加载");
        JButton saveBtn = new JButton("保存");
        filePanel.add(loadBtn);
        filePanel.add(saveBtn);
        controlPanel.add(filePanel);

        // === 优化策略 ===
        JPanel strategyPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        strategyPanel.setBorder(BorderFactory.createTitledBorder("优化策略"));
        String[] strategies = {"WEIGHT", "DELAY", "BANDWIDTH", "PACKET_LOSS", "RELIABILITY"};
        strategyBox = new JComboBox<>(strategies);
        strategyPanel.add(new JLabel("当前策略:"));
        strategyPanel.add(strategyBox);
        controlPanel.add(strategyPanel);

        // === 拓扑分析 ===
        JPanel analyzePanel = new JPanel(new GridLayout(1, 2, 5, 5));
        analyzePanel.setBorder(BorderFactory.createTitledBorder("拓扑分析"));
        JButton analyzeBtn = new JButton("分析关键节点");
        JButton clearAnalysisBtn = new JButton("清除分析");
        analyzePanel.add(analyzeBtn);
        analyzePanel.add(clearAnalysisBtn);
        controlPanel.add(analyzePanel);

        // === 其他操作 ===
        JPanel otherPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        otherPanel.setBorder(BorderFactory.createTitledBorder("其他"));
        JButton showBtn = new JButton("刷新图");
        JButton clearBtn = new JButton("清空图");
        otherPanel.add(showBtn);
        otherPanel.add(clearBtn);
        controlPanel.add(otherPanel);

        // === 自然语言输入 ===
        JPanel nlpPanel = new JPanel(new BorderLayout(5, 5));
        nlpPanel.setBorder(BorderFactory.createTitledBorder("自然语言输入"));
        nlpField = new JTextField();
        JButton nlpSendBtn = new JButton("发送");
        nlpPanel.add(nlpField, BorderLayout.CENTER);
        nlpPanel.add(nlpSendBtn, BorderLayout.EAST);
        controlPanel.add(nlpPanel);

        // 消息输出区
        messageArea = new JTextArea(8, 25);
        messageArea.setEditable(false);
        JScrollPane msgScrollPane = new JScrollPane(messageArea);
        msgScrollPane.setBorder(BorderFactory.createTitledBorder("消息输出"));
        msgScrollPane.setPreferredSize(new Dimension(300, 140));

        // 右侧面板：控制面板（可滚动） + 消息区
        JScrollPane controlScrollPane = new JScrollPane(controlPanel);
        controlScrollPane.setPreferredSize(new Dimension(320, 0));
        controlScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(controlScrollPane, BorderLayout.CENTER);
        rightPanel.add(msgScrollPane, BorderLayout.SOUTH);
        rightPanel.setPreferredSize(new Dimension(330, 0));

        add(rightPanel, BorderLayout.EAST);

        revalidate();
        repaint();

        // ----- 事件绑定 -----
        addDirectedBtn.addActionListener(e -> addDirectedEdge());
        addUndirectedBtn.addActionListener(e -> addUndirectedEdge());
        pathBtn.addActionListener(e -> queryShortestPath());
        allPathsBtn.addActionListener(e -> queryAllShortestPaths());
        kPathBtn.addActionListener(e -> queryKPaths());
        reachBtn.addActionListener(e -> queryReachable());
        constrainBtn.addActionListener(e -> queryConstrainedPath());
        clearConstrainBtn.addActionListener(e -> {
            viaField.setText("");
            avoidField.setText("");
            hopsField.setText("");
        });
        loadBtn.addActionListener(e -> loadTopology());
        saveBtn.addActionListener(e -> saveTopology());
        analyzeBtn.addActionListener(e -> runAnalyze());
        clearAnalysisBtn.addActionListener(e -> clearAnalysis());
        showBtn.addActionListener(e -> {
            refreshGraphAndClearHighlight();
            appendMessage("已刷新图形");
        });
        clearBtn.addActionListener(e -> clearGraph());
        strategyBox.addActionListener(e -> {
            String selected = (String) strategyBox.getSelectedItem();
            AgentResponse response = commandService.handle("strategy " + selected.toLowerCase());
            appendMessage(response.getMessage());
            // 策略切换后自动刷新当前高亮路径
            if (currentHighlightPath != null) {
                String lastSrc = commandService.getContext().getLastSource();
                String lastDst = commandService.getContext().getLastTarget();
                if (lastSrc != null && lastDst != null) {
                    AgentResponse r = commandService.handle("path " + lastSrc + " " + lastDst);
                    if (r.isSuccess()) {
                        currentHighlightPath = r.getPathResult();
                    }
                }
            }
            graphPanel.repaint();
        });
        nlpSendBtn.addActionListener(e -> handleNaturalLanguage());
        nlpField.addActionListener(e -> handleNaturalLanguage());
    }

    // ========== 操作方法 ==========

    private void addDirectedEdge() {
        String from = fromField.getText().trim();
        String to = toField.getText().trim();
        String weight = weightField.getText().trim();
        if (from.isEmpty() || to.isEmpty()) {
            appendError("节点名不能为空");
            return;
        }
        AgentResponse response = commandService.handle("add " + from + " " + to + " " + weight);
        if (response.isSuccess()) {
            appendMessage(response.getMessage());
            refreshGraphAndClearHighlight();
        } else {
            appendError(response.getMessage());
        }
    }

    private void addUndirectedEdge() {
        String from = fromField.getText().trim();
        String to = toField.getText().trim();
        String weight = weightField.getText().trim();
        if (from.isEmpty() || to.isEmpty()) {
            appendError("节点名不能为空");
            return;
        }
        AgentResponse response = commandService.handle("addud " + from + " " + to + " " + weight);
        if (response.isSuccess()) {
            appendMessage(response.getMessage());
            refreshGraphAndClearHighlight();
        } else {
            appendError(response.getMessage());
        }
    }

    private void queryShortestPath() {
        String src = srcField.getText().trim();
        String dst = dstField.getText().trim();
        if (src.isEmpty() || dst.isEmpty()) {
            appendError("起点和终点不能为空");
            return;
        }
        AgentResponse response = commandService.handle("path " + src + " " + dst);
        appendMessage(response.getMessage());
        if (response.isSuccess()) {
            currentHighlightPath = response.getPathResult();
        } else {
            currentHighlightPath = null;
        }
        graphPanel.repaint();
    }

    private void queryAllShortestPaths() {
        String src = srcField.getText().trim();
        String dst = dstField.getText().trim();
        if (src.isEmpty() || dst.isEmpty()) {
            appendError("起点和终点不能为空");
            return;
        }
        AgentResponse response = commandService.handle("allpaths " + src + " " + dst);
        appendMessage(response.getMessage());
        if (response.isSuccess()) {
            List<PathResult> results = response.getPathResults();
            if (!results.isEmpty()) {
                currentHighlightPath = choosePath(results);
            } else {
                currentHighlightPath = null;
            }
        } else {
            currentHighlightPath = null;
        }
        graphPanel.repaint();
    }

    private void queryKPaths() {
        String src = srcField.getText().trim();
        String dst = dstField.getText().trim();
        String kStr = kField.getText().trim();
        if (src.isEmpty() || dst.isEmpty()) {
            appendError("起点和终点不能为空");
            return;
        }
        int k;
        try {
            k = Integer.parseInt(kStr);
        } catch (NumberFormatException ex) {
            appendError("K 值必须是正整数");
            return;
        }
        AgentResponse response = commandService.handle("kpath " + src + " " + dst + " " + k);
        appendMessage(response.getMessage());
        if (response.isSuccess()) {
            List<PathResult> results = response.getPathResults();
            if (!results.isEmpty()) {
                currentHighlightPath = choosePath(results);
            } else {
                currentHighlightPath = null;
            }
        } else {
            currentHighlightPath = null;
        }
        graphPanel.repaint();
    }

    private void queryReachable() {
        String src = srcField.getText().trim();
        String dst = dstField.getText().trim();
        if (src.isEmpty() || dst.isEmpty()) {
            appendError("起点和终点不能为空");
            return;
        }
        AgentResponse response = commandService.handle("reach " + src + " " + dst);
        appendMessage(response.getMessage());
    }

    private void queryConstrainedPath() {
        String src = srcField.getText().trim();
        String dst = dstField.getText().trim();
        if (src.isEmpty() || dst.isEmpty()) {
            appendError("起点和终点不能为空（在上方路径查询区域填写）");
            return;
        }
        StringBuilder cmd = new StringBuilder("constrain path " + src + " " + dst);
        String via = viaField.getText().trim();
        String avoid = avoidField.getText().trim();
        String hops = hopsField.getText().trim();
        if (!via.isEmpty()) cmd.append(" via ").append(via);
        if (!avoid.isEmpty()) cmd.append(" avoid ").append(avoid);
        if (!hops.isEmpty()) cmd.append(" hops ").append(hops);

        AgentResponse response = commandService.handle(cmd.toString());
        appendMessage(response.getMessage());
        if (response.isSuccess()) {
            currentHighlightPath = response.getPathResult();
        } else {
            currentHighlightPath = null;
        }
        graphPanel.repaint();
    }

    private void runAnalyze() {
        AgentResponse response = commandService.handle("analyze");
        appendMessage(response.getMessage());
        if (response.isSuccess() && response.getAnalyzeResult() != null) {
            graphPanel.setAnalysisResult(response.getAnalyzeResult());
        }
        graphPanel.repaint();
    }

    private void clearAnalysis() {
        graphPanel.clearAnalysisResult();
        graphPanel.repaint();
        appendMessage("已清除分析高亮");
    }

    private void handleNaturalLanguage() {
        String text = nlpField.getText().trim();
        if (text.isEmpty()) return;
        nlpField.setText("");

        AgentResponse response = commandService.handle(text);
        if (response.getNormalizedCommand() != null
                && !response.getNormalizedCommand().equals(response.getOriginalInput())) {
            appendMessage("识别为: " + response.getNormalizedCommand());
        }
        appendMessage(response.getMessage());

        // 路径高亮
        if (response.isSuccess()) {
            if (response.getPathResult() != null) {
                currentHighlightPath = response.getPathResult();
            } else if (response.getPathResults() != null && !response.getPathResults().isEmpty()) {
                currentHighlightPath = choosePath(response.getPathResults());
            }
            // 分析结果可视化
            if (response.getAnalyzeResult() != null) {
                graphPanel.setAnalysisResult(response.getAnalyzeResult());
            }
        }

        // 同步策略 combo box
        syncStrategyCombo();

        // 刷新图（边标签可能因策略变化而更新）
        graphPanel.repaint();
    }

    private void syncStrategyCombo() {
        OptimizeStrategy current = agent.getStrategy();
        String name = current.name();
        for (int i = 0; i < strategyBox.getItemCount(); i++) {
            if (strategyBox.getItemAt(i).equals(name)) {
                if (strategyBox.getSelectedIndex() != i) {
                    strategyBox.setSelectedIndex(i);
                }
                return;
            }
        }
    }

    private PathResult choosePath(List<PathResult> results) {
        if (results.size() == 1) return results.get(0);
        String[] options = new String[results.size()];
        for (int i = 0; i < results.size(); i++) {
            options[i] = "路径 " + (i + 1) + ": " + String.join(" -> ", results.get(i).path)
                    + " (代价:" + results.get(i).totalCost + ")";
        }
        int choice = JOptionPane.showOptionDialog(this, "选择要高亮的路径", "多条路径",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        return (choice >= 0) ? results.get(choice) : results.get(0);
    }

    private void loadTopology() {
        String filename = fileField.getText().trim();
        if (filename.isEmpty()) {
            appendError("请输入文件名");
            return;
        }
        AgentResponse response = commandService.handle("load " + filename);
        if (response.isSuccess()) {
            appendMessage(response.getMessage());
            refreshGraphAndClearHighlight();
        } else {
            appendError(response.getMessage());
        }
    }

    private void saveTopology() {
        String filename = fileField.getText().trim();
        if (filename.isEmpty()) {
            appendError("请输入文件名");
            return;
        }
        AgentResponse response = commandService.handle("save " + filename);
        if (response.isSuccess()) {
            appendMessage(response.getMessage());
        } else {
            appendError(response.getMessage());
        }
    }

    private void clearGraph() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要清空所有节点和边吗？", "确认清空", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            agent.clear();
            refreshGraphAndClearHighlight();
            graphPanel.clearAnalysisResult();
            appendMessage("图已清空");
        }
    }

    private void refreshGraphAndClearHighlight() {
        currentHighlightPath = null;
        graphPanel.refreshGraph(agent);
        graphPanel.repaint();
    }

    private void appendMessage(String msg) {
        messageArea.append(msg + "\n");
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }

    private void appendError(String err) {
        messageArea.append("[错误] " + err + "\n");
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }

    public PathResult getCurrentHighlightPath() {
        return currentHighlightPath;
    }

    public PathOptimizerAgent getAgent() {
        return agent;
    }

    public void refreshUI() {
        graphPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NetworkPathGUI::new);
    }
}

// ========== 绘图面板 ==========

class GraphPanel extends JPanel {
    private PathOptimizerAgent agent;
    private NetworkPathGUI parent;
    private Map<String, Point2D> nodePositions;
    private int radius = 30;

    // 分析结果可视化
    private Set<String> criticalNodes = Collections.emptySet();
    private Set<String> bottleneckFrom = Collections.emptySet();
    private Set<String> bottleneckTo = Collections.emptySet();

    // 节点拖拽
    private String draggingNode = null;

    public GraphPanel(PathOptimizerAgent agent, NetworkPathGUI parent) {
        this.agent = agent;
        this.parent = parent;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 500));
        nodePositions = new HashMap<>();

        // 鼠标拖拽支持
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                draggingNode = findNodeAt(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggingNode = null;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggingNode != null) {
                    nodePositions.put(draggingNode, new Point2D.Double(e.getX(), e.getY()));
                    repaint();
                }
            }
        });
    }

    public void refreshGraph(PathOptimizerAgent agent) {
        this.agent = agent;
        calculateNodePositions();
    }

    public void setAnalysisResult(TopologyAnalysisResult result) {
        criticalNodes = new HashSet<>(result.criticalNodes);
        bottleneckFrom = new HashSet<>();
        bottleneckTo = new HashSet<>();
        for (String edge : result.bottleneckEdges) {
            // 格式: "A -> B (权重5, 通过3次)"
            String[] parts = edge.split("\\s*->\\s*");
            if (parts.length >= 2) {
                bottleneckFrom.add(parts[0].trim());
                String right = parts[1].trim();
                // 提取目标节点名（去掉括号部分）
                int parenIdx = right.indexOf('(');
                String target = parenIdx > 0 ? right.substring(0, parenIdx).trim() : right.trim();
                bottleneckTo.add(target);
            }
        }
    }

    public void clearAnalysisResult() {
        criticalNodes = Collections.emptySet();
        bottleneckFrom = Collections.emptySet();
        bottleneckTo = Collections.emptySet();
    }

    private String findNodeAt(int mx, int my) {
        for (Map.Entry<String, Point2D> entry : nodePositions.entrySet()) {
            Point2D p = entry.getValue();
            if (p.distance(mx, my) <= radius) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void calculateNodePositions() {
        if (agent == null) return;
        Map<String, List<Edge>> graph = agent.getGraph();
        // 保留已有节点的位置（拖拽后不重置）
        Set<String> existingNodes = new HashSet<>(nodePositions.keySet());
        Set<String> currentNodes = graph.keySet();

        // 移除不再存在的节点
        nodePositions.keySet().retainAll(currentNodes);

        // 计算新增节点的位置
        List<String> newNodes = new ArrayList<>();
        for (String node : currentNodes) {
            if (!nodePositions.containsKey(node)) {
                newNodes.add(node);
            }
        }

        if (newNodes.isEmpty()) return;

        int n = currentNodes.size();
        int width = getWidth() - 100;
        int height = getHeight() - 100;
        if (width <= 0) width = 500;
        if (height <= 0) height = 400;
        double cx = width / 2.0 + 50;
        double cy = height / 2.0 + 50;
        double rx = width / 2.2;
        double ry = height / 2.2;

        // 将新节点放入空位
        int i = 0;
        for (String node : currentNodes) {
            if (!nodePositions.containsKey(node)) {
                double angle = 2 * Math.PI * i / n;
                double x = cx + rx * Math.cos(angle);
                double y = cy + ry * Math.sin(angle);
                nodePositions.put(node, new Point2D.Double(x, y));
            }
            i++;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (agent == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (nodePositions.isEmpty() && agent.getGraph() != null && !agent.getGraph().isEmpty()) {
            calculateNodePositions();
        }

        Map<String, List<Edge>> graph = agent.getGraph();
        if (graph == null || graph.isEmpty()) {
            g2.drawString("图为空", 50, 50);
            return;
        }

        PathResult highlight = parent.getCurrentHighlightPath();

        // 1. 绘制边
        for (Map.Entry<String, List<Edge>> entry : graph.entrySet()) {
            String from = entry.getKey();
            Point2D fromP = nodePositions.get(from);
            if (fromP == null) continue;
            for (Edge e : entry.getValue()) {
                String to = e.target;
                Point2D toP = nodePositions.get(to);
                if (toP == null) continue;

                boolean isPathHighlight = false;
                if (highlight != null && highlight.path != null) {
                    for (int i = 0; i < highlight.path.size() - 1; i++) {
                        if (highlight.path.get(i).equals(from) && highlight.path.get(i + 1).equals(to)) {
                            isPathHighlight = true;
                            break;
                        }
                    }
                }

                boolean isBottleneck = bottleneckFrom.contains(from) && bottleneckTo.contains(to);

                if (isPathHighlight) {
                    g2.setColor(Color.RED);
                    g2.setStroke(new BasicStroke(3));
                } else if (isBottleneck) {
                    g2.setColor(Color.ORANGE);
                    g2.setStroke(new BasicStroke(3));
                } else {
                    g2.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke(1));
                }

                // 边标签使用当前策略下的代价
                int cost = e.getCost(agent.getStrategy());
                drawArrowLine(g2, fromP, toP, cost, isPathHighlight || isBottleneck);
            }
        }

        // 2. 绘制节点
        for (Map.Entry<String, Point2D> entry : nodePositions.entrySet()) {
            String node = entry.getKey();
            Point2D p = entry.getValue();
            boolean isPathNode = highlight != null && highlight.path != null && highlight.path.contains(node);
            boolean isCritical = criticalNodes.contains(node);

            if (isPathNode) {
                g2.setColor(Color.RED);
                g2.fillOval((int) (p.getX() - radius / 2), (int) (p.getY() - radius / 2), radius, radius);
                g2.setColor(Color.WHITE);
            } else if (isCritical) {
                g2.setColor(Color.ORANGE);
                g2.fillOval((int) (p.getX() - radius / 2), (int) (p.getY() - radius / 2), radius, radius);
                g2.setColor(Color.BLACK);
            } else {
                g2.setColor(Color.LIGHT_GRAY);
                g2.fillOval((int) (p.getX() - radius / 2), (int) (p.getY() - radius / 2), radius, radius);
                g2.setColor(Color.BLACK);
            }
            g2.drawOval((int) (p.getX() - radius / 2), (int) (p.getY() - radius / 2), radius, radius);
            FontMetrics fm = g2.getFontMetrics();
            int sw = fm.stringWidth(node);
            int sh = fm.getHeight();
            g2.drawString(node, (int) (p.getX() - sw / 2), (int) (p.getY() + sh / 4));
        }
    }

    private void drawArrowLine(Graphics2D g2, Point2D from, Point2D to, int weight, boolean highlight) {
        double angle = Math.atan2(to.getY() - from.getY(), to.getX() - from.getX());
        int arrowSize = 10;
        double offset = radius / 2.0;
        double startX = from.getX() + Math.cos(angle) * offset;
        double startY = from.getY() + Math.sin(angle) * offset;
        double endX = to.getX() - Math.cos(angle) * offset;
        double endY = to.getY() - Math.sin(angle) * offset;
        g2.drawLine((int) startX, (int) startY, (int) endX, (int) endY);

        double arrowAngle = Math.PI / 6;
        double x1 = endX - arrowSize * Math.cos(angle - arrowAngle);
        double y1 = endY - arrowSize * Math.sin(angle - arrowAngle);
        double x2 = endX - arrowSize * Math.cos(angle + arrowAngle);
        double y2 = endY - arrowSize * Math.sin(angle + arrowAngle);
        g2.fillPolygon(new int[]{(int) endX, (int) x1, (int) x2},
                new int[]{(int) endY, (int) y1, (int) y2}, 3);

        double midX = (startX + endX) / 2;
        double midY = (startY + endY) / 2;
        g2.setColor(highlight ? Color.RED : Color.DARK_GRAY);
        g2.drawString(String.valueOf(weight), (int) midX, (int) midY - 5);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        calculateNodePositions();
    }
}
