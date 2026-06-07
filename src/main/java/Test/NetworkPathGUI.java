package Test;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import Test.PathOptimizerAgent.PathResult;

/**
 * 网络路径寻优智能体 - 图形界面版
 */
public class NetworkPathGUI extends JFrame {
    private PathOptimizerAgent agent;
    private AgentCommandService commandService;
    private GraphPanel graphPanel;
    private JTextArea messageArea;
    private JTextField srcField, dstField, fromField, toField, weightField, fileField;
    private PathResult currentHighlightPath;   // 当前高亮的最短路径

    public NetworkPathGUI() {
        agent = new PathOptimizerAgent();
        commandService = new AgentCommandService(agent);
        // 预添加一些演示节点（可选），可注释
        initDemo();
        initUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initDemo() {
        // 演示：通过命令服务添加节点和边
        commandService.handle("add A B 5");
        commandService.handle("add A C 2");
        commandService.handle("add B D 1");
        commandService.handle("add C B 3");
        commandService.handle("add C D 4");
        commandService.handle("add D E 2");
    }

    private void initUI() {
        setTitle("网络路径寻优智能体");
        setLayout(new BorderLayout());

        // 中央绘图面板
        graphPanel = new GraphPanel(agent, this);
        add(graphPanel, BorderLayout.CENTER);

        // 右侧控制面板（可以使用多个面板，这里放在右侧）
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("操作面板"));
        controlPanel.setMinimumSize(new Dimension(280, 0));   // 确保最小宽度

        // 添加边区域
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

        // 查询区域
        JPanel queryPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        queryPanel.setBorder(BorderFactory.createTitledBorder("路径查询"));
        queryPanel.add(new JLabel("起点:"));
        srcField = new JTextField(8);
        queryPanel.add(srcField);
        queryPanel.add(new JLabel("终点:"));
        dstField = new JTextField(8);
        queryPanel.add(dstField);
        JButton pathBtn = new JButton("最短路径");
        JButton allPathsBtn = new JButton("所有最短路径");
        queryPanel.add(pathBtn);
        queryPanel.add(allPathsBtn);
        controlPanel.add(queryPanel);

        // 文件操作
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

        // 其他操作
        JPanel otherPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        otherPanel.setBorder(BorderFactory.createTitledBorder("其他"));
        JButton showBtn = new JButton("显示图");
        JButton clearBtn = new JButton("清空图");
        otherPanel.add(showBtn);
        otherPanel.add(clearBtn);
        controlPanel.add(otherPanel);

        // 消息输出区
        messageArea = new JTextArea(10, 25);
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("消息输出"));
        scrollPane.setMinimumSize(new Dimension(280, 120));
        scrollPane.setPreferredSize(new Dimension(300, 150));

        // 右侧总面板 (垂直排列)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(controlPanel, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.setMinimumSize(new Dimension(300, 600));
        rightPanel.setPreferredSize(new Dimension(300, 600));

        add(rightPanel, BorderLayout.EAST);

        // 强制重新计算布局
        revalidate();
        repaint();

        // ----- 事件绑定 -----
        addDirectedBtn.addActionListener(e -> addDirectedEdge());
        addUndirectedBtn.addActionListener(e -> addUndirectedEdge());
        pathBtn.addActionListener(e -> queryShortestPath());
        allPathsBtn.addActionListener(e -> queryAllShortestPaths());
        loadBtn.addActionListener(e -> loadTopology());
        saveBtn.addActionListener(e -> saveTopology());
        showBtn.addActionListener(e -> {
            refreshGraphAndClearHighlight();
            appendMessage("已刷新图形");
        });
        clearBtn.addActionListener(e -> clearGraph());
    }

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
                String[] options = new String[results.size()];
                for (int i = 0; i < results.size(); i++) options[i] = "路径 " + (i + 1);
                int choice = JOptionPane.showOptionDialog(this, "选择要高亮显示的路径", "多条最短路径",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                currentHighlightPath = (choice >= 0) ? results.get(choice) : results.get(0);
            } else {
                currentHighlightPath = null;
            }
        } else {
            currentHighlightPath = null;
        }
        graphPanel.repaint();
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
        int confirm = JOptionPane.showConfirmDialog(this, "确定要清空所有节点和边吗？", "确认清空", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            agent.clear();
            refreshGraphAndClearHighlight();
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

    // 提供外部访问当前高亮路径的方法
    public PathResult getCurrentHighlightPath() {
        return currentHighlightPath;
    }

    // 刷新绘图（供 GraphPanel 调用）
    public void refreshUI() {
        graphPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NetworkPathGUI::new);
    }
}

/**
 * 绘图面板，根据图结构绘制节点和边
 */
class GraphPanel extends JPanel {
    private PathOptimizerAgent agent;
    private NetworkPathGUI parent;
    private Map<String, Point2D> nodePositions; // 节点位置缓存
    private int radius = 30; // 节点半径

    public GraphPanel(PathOptimizerAgent agent, NetworkPathGUI parent) {
        this.agent = agent;
        this.parent = parent;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 500));
        nodePositions = new HashMap<>();
    }

    /**
     * 刷新图引用并重新计算节点位置
     */
    public void refreshGraph(PathOptimizerAgent agent) {
        this.agent = agent;
        calculateNodePositions();
    }

    /**
     * 圆形布局：根据节点数量均匀分布在椭圆上
     */
    private void calculateNodePositions() {
        if (agent == null) return;
        Map<String, List<Edge>> graph = agent.getGraph(); // 需要添加 getter
        nodePositions.clear();
        Set<String> nodes = graph.keySet();
        if (nodes.isEmpty()) return;
        int n = nodes.size();
        int width = getWidth() - 100;
        int height = getHeight() - 100;
        if (width <= 0) width = 500;
        if (height <= 0) height = 400;
        double cx = width / 2.0 + 50;
        double cy = height / 2.0 + 50;
        double rx = width / 2.2;
        double ry = height / 2.2;
        int i = 0;
        for (String node : nodes) {
            double angle = 2 * Math.PI * i / n;
            double x = cx + rx * Math.cos(angle);
            double y = cy + ry * Math.sin(angle);
            nodePositions.put(node, new Point2D.Double(x, y));
            i++;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (agent == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 确保节点位置已计算（仅在缓存为空时计算）
        if (nodePositions.isEmpty() && agent.getGraph() != null && !agent.getGraph().isEmpty()) {
            calculateNodePositions();
        }

        Map<String, List<Edge>> graph = agent.getGraph();
        if (graph == null || graph.isEmpty()) {
            g2.drawString("图为空", 50, 50);
            return;
        }

        // 1. 绘制边（有向边带箭头）
        g2.setStroke(new BasicStroke(2));
        for (Map.Entry<String, List<Edge>> entry : graph.entrySet()) {
            String from = entry.getKey();
            Point2D fromP = nodePositions.get(from);
            if (fromP == null) continue;
            for (Edge e : entry.getValue()) {
                String to = e.target;
                Point2D toP = nodePositions.get(to);
                if (toP == null) continue;
                // 判断是否在高亮路径中
                boolean isHighlight = false;
                PathResult highlight = parent.getCurrentHighlightPath();
                if (highlight != null && highlight.path != null) {
                    for (int i = 0; i < highlight.path.size() - 1; i++) {
                        if (highlight.path.get(i).equals(from) && highlight.path.get(i+1).equals(to)) {
                            isHighlight = true;
                            break;
                        }
                    }
                }
                if (isHighlight) {
                    g2.setColor(Color.RED);
                    g2.setStroke(new BasicStroke(3));
                } else {
                    g2.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke(1));
                }
                drawArrowLine(g2, fromP, toP, e.weight, isHighlight);
            }
        }

        // 2. 绘制节点
        for (Map.Entry<String, Point2D> entry : nodePositions.entrySet()) {
            String node = entry.getKey();
            Point2D p = entry.getValue();
            boolean isHighlight = false;
            PathResult highlight = parent.getCurrentHighlightPath();
            if (highlight != null && highlight.path != null && highlight.path.contains(node)) {
                isHighlight = true;
            }
            if (isHighlight) {
                g2.setColor(Color.RED);
                g2.fillOval((int)(p.getX() - radius/2), (int)(p.getY() - radius/2), radius, radius);
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(Color.LIGHT_GRAY);
                g2.fillOval((int)(p.getX() - radius/2), (int)(p.getY() - radius/2), radius, radius);
                g2.setColor(Color.BLACK);
            }
            g2.drawOval((int)(p.getX() - radius/2), (int)(p.getY() - radius/2), radius, radius);
            FontMetrics fm = g2.getFontMetrics();
            int sw = fm.stringWidth(node);
            int sh = fm.getHeight();
            g2.drawString(node, (int)(p.getX() - sw/2), (int)(p.getY() + sh/4));
        }
    }

    /**
     * 绘制带权重的有向边，如果高亮则红色加粗
     */
    private void drawArrowLine(Graphics2D g2, Point2D from, Point2D to, int weight, boolean highlight) {
        double angle = Math.atan2(to.getY() - from.getY(), to.getX() - from.getX());
        int arrowSize = 10;
        // 起点和终点偏移（避免箭头覆盖节点）
        double offset = radius / 2.0;
        double startX = from.getX() + Math.cos(angle) * offset;
        double startY = from.getY() + Math.sin(angle) * offset;
        double endX = to.getX() - Math.cos(angle) * offset;
        double endY = to.getY() - Math.sin(angle) * offset;
        g2.drawLine((int)startX, (int)startY, (int)endX, (int)endY);

        // 画箭头
        double arrowAngle = Math.PI / 6;
        double x1 = endX - arrowSize * Math.cos(angle - arrowAngle);
        double y1 = endY - arrowSize * Math.sin(angle - arrowAngle);
        double x2 = endX - arrowSize * Math.cos(angle + arrowAngle);
        double y2 = endY - arrowSize * Math.sin(angle + arrowAngle);
        g2.fillPolygon(new int[]{(int)endX, (int)x1, (int)x2},
                new int[]{(int)endY, (int)y1, (int)y2}, 3);

        // 绘制权重文本（放在边的中点附近）
        double midX = (startX + endX) / 2;
        double midY = (startY + endY) / 2;
        g2.drawString(String.valueOf(weight), (int)midX, (int)midY - 5);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        calculateNodePositions();
    }
}
