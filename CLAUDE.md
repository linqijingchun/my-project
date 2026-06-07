# NetTest - 网络路径寻优智能体

## 项目概览

面向网络拓扑管理场景的路径寻优智能体原型。有向加权图表示网络拓扑，核心算法 Dijkstra，支持 CLI 和 Swing GUI 两种交互方式。

- 语言：Java 8+
- 构建：Maven 3.8+
- 测试：JUnit 5
- 仓库：https://github.com/linqijingchun/my-project
- 当前版本：1.4（K 条最短路径已完成）

## 编译与运行

```bash
mvn clean test                              # 编译+测试
mvn package                                 # 打包
java -cp target/classes Test.Main           # 运行 CLI
java -cp target/classes Test.NetworkPathGUI # 运行 GUI
```

## 项目结构

```
src/main/java/Test/
├── PathOptimizerAgent.java    # 核心：图结构、Dijkstra、文件I/O、拓扑摘要、拓扑分析
├── Edge.java                  # 边数据类（支持多维指标）
├── LinkMetrics.java           # 链路指标封装（时延/带宽/丢包/可靠性）
├── OptimizeStrategy.java      # 优化策略枚举
├── NodeDist.java              # 优先队列节点
├── TopologySummary.java       # 拓扑摘要数据类
│                              # （TopologyAnalysisResult 为 PathOptimizerAgent 内部类）
├── Intent.java                # 结构化意图对象（工厂方法模式）
├── IntentType.java            # 意图类型枚举
├── IntentParser.java          # 自然语言 + 标准命令解析
├── AgentCommandService.java   # 命令调度层（统一入口）
├── AgentContext.java          # 上下文记忆
├── AgentResponse.java         # 统一响应对象
├── Main.java                  # CLI 入口
└── NetworkPathGUI.java        # Swing GUI + GraphPanel 内部类

src/test/java/Test/
├── AgentTest.java             # 智能体集成测试（35个）
└── PathOptimizerAgentTest.java # 核心算法单元测试（28个）
```

## 代码规范

- 包名：Test（历史原因，暂不改动）
- 核心类为包级访问权限（package-private），不加 public
- 数据类字段用 final + 不可变集合
- 工厂方法命名语义化：Intent.addDirected()、Intent.path()
- 错误处理：Agent 层抛 IllegalArgumentException，Service 层统一捕获包装为 AgentResponse.error()

## 测试要求

- 每次修改核心逻辑后 mvn clean test 全量通过
- 新增功能需配套测试用例
- 重点覆盖：边界条件、异常路径、不可达、重复操作

## 工作路线图

按优先级排序，执行顺序：P0 → P1 → P2 → P3 → P4 → P5

| 优先级 | 任务 | 状态 | 说明 |
|--------|------|------|------|
| P0 | GUI 统一到 AgentCommandService | 已完成 | GUI 所有操作改为通过 Service 层执行 |
| P1 | 增加 analyze 命令 | 已完成 | 关键节点分析 + 瓶颈链路识别 |
| P2 | 增加约束路径查询 | 已完成 | 必经节点、避开节点、最大跳数（via/avoid/hops） |
| P3 | 链路指标多维扩展 | 已完成 | 时延、带宽、丢包率、可靠性 + 优化策略切换 |
| P4 | K 条最短路径 | 已完成 | Yen's algorithm，查询前 K 条最短路径 |
| P5 | GUI 可视化增强 | 待做 | 分析结果展示、自然语言输入 |

## 已知待改进项

- GUI 的 analyze 结果未在图上可视化高亮
- 瓶颈评分公式对 passCount=0 的边无法区分（score 恒为 0）
- 拓扑文件编码已统一为 UTF-8，旧 GBK 文件不兼容

## Git 工作流

- 主分支：main
- 提交格式：<type>: <description>（feat/fix/refactor/test/docs/chore）
- 推送前确保 mvn clean test 通过
- 推送：git push origin main

## 协作注意事项

- 修改代码前先 Read 理解现状，不要凭假设改动
- 每次改动小而聚焦，避免大范围重构
- Edit 的 old_string 必须与文件内容完全匹配（含缩进和空行）
- 文件末尾编辑匹配困难时，可用 Bash 追加但注意闭合花括号
- 修改完成后必须运行测试验证
