# 网络路径寻优智能体

这是一个面向网络拓扑管理场景的路径寻优智能体原型。系统使用有向加权图表示网络拓扑，能够读取或构建网络拓扑，分析节点、链路、连通性和最优路径，并支持用户通过标准命令或自然语言完成路径查询、拓扑修改、结果解释和后续优化分析。

当前项目基于 Java SE 开发，提供命令行和 Swing 图形界面两种使用方式。项目核心算法采用 Dijkstra 最短路径算法，当前智能体层已经具备基础自然语言解析、命令调度、统一响应对象和上下文记忆能力。用户既可以输入标准命令，也可以输入常见自然语言描述，例如“添加B到D长度为1”“帮我查询A到D的最短路径”或“为什么”。

## 项目目标

本项目的目标不是把程序直接做成真实网络中的路由器或交换机，而是构建一个网络拓扑分析与路径优化决策智能体。它的角色更接近网络管理员助手，能够围绕当前拓扑完成以下任务：

- 感知并维护当前网络拓扑，包括节点、链路、权重和连通关系
- 根据用户目标执行路径寻优、可达性判断和拓扑修改
- 通过自然语言交互降低使用门槛
- 记忆最近的查询上下文，支持“为什么”“解释一下”等追问
- 给出路径结果、总代价、备选路径和后续优化建议
- 通过图形界面展示拓扑结构和路径高亮结果

## 功能特性

- 支持添加有向边和无向边
- 支持修改、删除网络边
- 支持查询两点之间的最短路径
- 支持判断两个节点是否可达
- 支持查询多条等价最短路径
- 支持解释最近一次路径查询
- 支持从文本文件加载拓扑
- 支持将当前拓扑保存到文本文件
- 提供 Swing 图形界面展示节点、边、权重和高亮路径
- 支持统一响应对象 `AgentResponse`
- 支持智能体指令调度 `AgentCommandService`
- 支持基础自然语言解析 `IntentParser`
- 支持基础上下文记忆 `AgentContext`
- 支持 `help` 指令查看使用指南
- 支持约束路径查询（必经节点、避开节点、最大跳数）
- 支持链路多维指标（时延、带宽、丢包率、可靠性）
- 支持优化策略切换（按时延/带宽/丢包率/可靠性/综合权重寻路）
- 支持查询前 K 条最短路径（Yen's algorithm）

## 项目结构

```text
netTest
├── pom.xml
├── README.md
└── src
    ├── main
    │   └── java
    │       └── Test
    │           ├── AgentCommandService.java
    │           ├── AgentContext.java
    │           ├── AgentResponse.java
    │           ├── Edge.java
    │           ├── IntentParser.java
    │           ├── Main.java
    │           ├── NetworkPathGUI.java
    │           ├── NodeDist.java
    │           └── PathOptimizerAgent.java
    └── test
        └── java
            └── Test
                ├── AgentTest.java
                └── PathOptimizerAgentTest.java
```

## 环境要求

- JDK 8 或更高版本
- Maven 3.8 或更高版本
- 推荐使用 IntelliJ IDEA 打开项目

当前项目已经迁移为 Maven 标准工程，推荐通过 Maven 编译、测试和运行。

## 编译与测试

运行全部 JUnit 测试：

```powershell
mvn test
```

从零清理并测试：

```powershell
mvn clean test
```

打包：

```powershell
mvn package
```

## 运行命令行版本

先编译：

```powershell
mvn package
```

再运行：

```powershell
java -cp target\classes Test.Main
```

启动后可以输入命令管理网络拓扑：

```text
add A B 5
add A C 2
add C D 4
path A D
allpaths A D
show
help
exit
```

常用命令：

```text
add <from> <to> <weight>        添加有向边
addud <from> <to> <weight>      添加无向边
path <src> <dst>                查询最短路径
reach <src> <dst>               判断是否可达
update <from> <to> <weight>     修改边的权重
remove <from> <to>              删除一条边
show                            显示当前图
load <filename>                 从文件加载拓扑
save <filename>                 保存拓扑到文件
allpaths <src> <dst>            查询所有最短路径
kpath <src> <dst> <K>           查询前 K 条最短路径
constrain path <src> <dst>      约束路径查询
  [via <n1,n2>] [avoid <n3>] [hops <N>]
strategy <type>                 切换优化策略
  (weight/delay/bandwidth/loss/reliability)
add <from> <to> <w> [指标...]   添加带指标的边
  delay <ms> bandwidth <Mbps> loss <%> reliability <%>
analyze                         分析关键节点和瓶颈链路
summary                         输出拓扑摘要
explain                         解释最近一次路径查询
help                            查看使用指南
exit                            退出程序
```

也可以输入常见自然语言：

```text
添加B到D长度为1
添加无向边A到B长度为3
帮我查询A到D的最短路径
查询A到D的所有最短路径
查询A到D的3条最短路径
A到D能不能到
把A到B的权重改成4
删除A到B的边
显示当前拓扑
为什么
解释一下
从A到D经过B的最短路径
从A到D避开C的路径
从A到D跳数不超过3的路径
设置优化策略为时延
以带宽为优化目标
```

## 运行图形界面版本

```powershell
java -cp target\classes Test.NetworkPathGUI
```

图形界面提供：

- 拓扑可视化
- 添加有向边/无向边
- 查询最短路径
- 查询所有等价最短路径
- 加载和保存拓扑文件
- 清空当前图

## 拓扑文件格式

拓扑文件采用纯文本格式，每行表示一条有向边，权重必须是正整数：

```text
from to weight
```

示例：

```text
A B 5
A C 2
C B 3
C D 4
D E 2
```

以 `#` 开头的行会被视为注释。

## 核心算法

项目核心类是 `PathOptimizerAgent`，使用邻接表保存拓扑：

```text
Map<String, List<Edge>>
```

最短路径查询使用 Dijkstra 算法，适合处理正权重的网络路径寻优场景。路径总代价由每条边的权重累加得到，权重可以表示时延、跳数、链路成本或综合代价。

### 约束路径查询

支持在最短路径基础上添加三种约束：

- **必经节点（via）**：路径必须经过指定节点，多个节点用逗号分隔（最多 3 个）。算法对 via 节点全排列后分段 Dijkstra，取总代价最小的组合。
- **避开节点（avoid）**：路径不得经过指定节点。算法临时从图中移除被避开节点及其关联边后执行 Dijkstra。
- **跳数限制（hops）**：路径跳数不超过指定值。算法使用状态空间 Dijkstra，在 `(node, hops)` 状态上做最短路。

三种约束可组合使用，例如：

```text
constrain path A D via B,C avoid E hops 5
```

该命令查询从 A 到 D、必须依次经过 B 和 C（或 C 和 B 中代价更小的排列）、不经过 E、且跳数不超过 5 的最短路径。

**设计说明**：当前实现采用方案 A（在 Intent 中扩展 via/avoid/hops 字段），因为约束数量有限（3 种），直接扩展字段更简单直观。若未来约束类型大幅增加，可重构为方案 B（将每种约束抽象为 Constraint 接口，Intent 持有 `List<Constraint>`），以获得更好的扩展性。

### 链路指标多维扩展

支持为每条边附加四维网络指标：

| 指标 | 含义 | 单位 | 优化方向 |
|------|------|------|----------|
| delay | 时延 | ms | 越小越好 |
| bandwidth | 带宽 | Mbps | 越大越好 |
| packetLoss | 丢包率 | % | 越小越好 |
| reliability | 可靠性 | % | 越大越好 |

**添加带指标的边**：

```text
add A B 5 delay 10 bandwidth 100 loss 0.5 reliability 99.9
```

四个指标必须同时提供。不提供指标时，边仅使用 weight 作为综合代价（完全兼容旧逻辑）。

**切换优化策略**：

```text
strategy delay      # 按时延寻路
strategy bandwidth  # 按带宽寻路（自动转换为代价）
strategy loss       # 按丢包率寻路
strategy reliability # 按可靠性寻路
strategy weight     # 恢复默认（综合权重）
```

Dijkstra 算法内部通过 `Edge.getCost(strategy)` 获取当前策略下的代价值。带宽和可靠性是"越大越好"指标，算法自动转换为"越小越好"的代价形式。

**文件格式扩展**：

```text
# 旧格式（兼容）
A B 5

# 新格式（7列）
A B 5 10 100 0.5 99.9
# from to weight delay bandwidth packetLoss reliability
```

加载时根据列数自动判断格式（3 列旧格式 / 7 列新格式）。

**设计说明**：当前采用组合模式（Edge 持有 LinkMetrics 引用），weight 字段保留作为综合代价。所有 Dijkstra 变体（基础/跳数限制/避开节点/约束路径）统一使用 `e.getCost(strategy)` 计算代价，策略切换对上层透明。

### K 条最短路径（Yen's algorithm）

除了单一最短路径外，系统还支持查询前 K 条不重复的最短路径。算法基于 Yen's algorithm：

1. 用 Dijkstra 求出第 1 条最短路径 P1
2. 对 P1 的每个中间节点（spur node），临时移除与已选路径重叠的边，在 spur node 处重新 Dijkstra 求偏离路径（spur path）
3. 将 root path + spur path 拼接为候选路径，加入候选集
4. 从候选集中取出代价最小且不重复的路径作为下一条结果
5. 重复直到找到 K 条路径或候选集为空

命令：

```text
kpath <src> <dst> <K>       查询前 K 条最短路径
```

自然语言示例：

```text
查询A到D的3条最短路径
```

返回结果按代价升序排列，K=1 时等价于普通最短路径查询。

## 智能体结构

当前版本将命令行交互、自然语言解析、命令调度、上下文记忆、核心算法和响应结果拆分为相对独立的模块：

```text
Main
读取用户输入，处理 exit，展示结果。

IntentParser
把常见自然语言转换成标准命令。

AgentCommandService
解析标准命令，调度 PathOptimizerAgent，并包装 AgentResponse。

AgentContext
记录最近一次命令和路径查询结果，为解释和连续追问提供上下文。

PathOptimizerAgent
维护拓扑结构，执行路径寻优、可达性判断、边操作和文件读写。

AgentResponse
统一返回执行结果，方便命令行、GUI 或未来 API 复用。
```

## 版本历史

### 1.4 版本 — K 条最短路径

- 实现 Yen's algorithm，支持查询前 K 条不重复的最短路径
- 新增 `kpath` 命令和自然语言"查询X到Y的N条最短路径"
- 新增 9 个测试用例（总计 63 个）

### 1.3 版本 — 链路指标多维扩展

- 新增 `LinkMetrics` 链路指标封装（时延、带宽、丢包率、可靠性）
- 新增 `OptimizeStrategy` 优化策略枚举
- 新增 `strategy` 命令，支持运行时切换优化策略
- `add` 命令扩展为可选附加四维指标
- 所有 Dijkstra 变体统一使用 `Edge.getCost(strategy)` 计算代价
- 文件格式扩展为 7 列（兼容旧 3 列格式）
- GUI 新增优化策略下拉选择框
- 新增 15 个测试用例（总计 54 个）

### 1.2 版本 — 约束路径查询

- 新增 `constrain path` 命令，支持 via（必经节点）、avoid（避开节点）、hops（跳数限制）
- 实现分段 Dijkstra（via 排列枚举）、临时图移除（avoid）、状态空间 Dijkstra（hops）
- 支持自然语言解析（”从A到D经过B”、”避开C”、”跳数不超过3”）
- via 节点限制最多 3 个
- 新增 14 个测试用例

### 1.1 版本 — Intent 化重构 + 智能体架构

- 拆分 `Edge`、`NodeDist`、`PathOptimizerAgent` 为独立文件
- 新增 `AgentCommandService` 指令调度层
- 新增 `AgentResponse` 统一响应对象
- 新增 `IntentParser` 自然语言 + 标准命令解析器
- 新增 `Intent` 结构化意图对象（工厂方法模式）
- 新增 `AgentContext` 上下文记忆，支持 `explain`/`why` 追问
- 新增 `summary`/`topology` 拓扑摘要命令
- 新增 `analyze` 关键节点 + 瓶颈链路分析命令
- 新增 `help` 使用指南
- 简化 `Main`，GUI 统一到 `AgentCommandService`
- 修复重复边检查、文件编码（UTF-8）、`NumberFormatException` 等问题

### 1.0 版本 — 基础原型

- 建立有向加权图拓扑数据结构（邻接表）
- 实现 Dijkstra 最短路径算法
- 提供 CLI 命令行交互方式
- 提供 Swing 图形界面（拓扑可视化、路径高亮）
- 支持拓扑文件加载与保存
- 支持多条等价最短路径查询

## 后续规划

| 优先级 | 任务 | 状态 | 说明 |
|--------|------|------|------|
| P5 | GUI 可视化增强 | 待做 | 分析结果展示、自然语言输入、指标可视化 |

### P5：GUI 可视化增强

- 在图形界面中展示 analyze 分析结果（关键节点高亮、瓶颈链路标红）
- GUI 支持自然语言输入框
- 策略切换后自动刷新路径高亮
- 增加示例拓扑文件和运行截图
