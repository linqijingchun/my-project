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

## 项目结构

```text
netTest
├── NetTest.iml
├── README.md
└── src
    └── Test
        ├── AgentCommandService.java
        ├── AgentContext.java
        ├── AgentResponse.java
        ├── AgentTestRunner.java
        ├── Edge.java
        ├── IntentParser.java
        ├── Main.java
        ├── NetworkPathGUI.java
        ├── NodeDist.java
        └── PathOptimizerAgent.java
```

## 环境要求

- JDK 8 或更高版本
- 推荐使用 IntelliJ IDEA 打开项目

当前项目没有使用 Maven 或 Gradle，直接通过 `javac` 和 `java` 即可编译运行。

## 编译方式

如果本机运行环境是 JDK 8，可以使用：

```powershell
javac -encoding UTF-8 -d out src\Test\*.java
```

如果编译器版本高于运行时版本，例如使用 JDK 21 编译、JDK 8 运行，建议指定 Java 8 目标版本：

```powershell
javac --release 8 -encoding UTF-8 -d out src\Test\*.java
```

## 运行命令行版本

```powershell
java -cp out Test.Main
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
A到D能不能到
把A到B的权重改成4
删除A到B的边
显示当前拓扑
为什么
解释一下
```

## 运行图形界面版本

```powershell
java -cp out Test.NetworkPathGUI
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

## 当前版本更新

- 新增 `AgentContext` 上下文记忆对象
- 新增 `explain` 命令，用于解释最近一次最短路径查询
- 支持自然语言追问，例如 `why`、`为什么`、`解释一下`
- 扩展 `AgentTestRunner`，增加上下文记忆和解释能力测试
- 明确项目方向为网络拓扑分析与路径优化决策智能体

## 1.1 版本更新

- 拆分 `Edge`、`NodeDist`、`PathOptimizerAgent` 为独立文件
- 新增 `AgentCommandService` 指令调度层
- 新增 `AgentResponse` 统一响应对象
- 新增 `IntentParser` 基础自然语言解析器
- 简化 `Main`，使其只负责基础命令行交互
- 新增 `help` 使用指南
- 支持常见自然语言控制网络路径智能体

## 1.0 版本定位

1.0 版本是网络路径寻优项目的基础原型，重点完成以下目标：

- 建立网络拓扑数据结构
- 实现基础路径寻优能力
- 提供命令行交互方式
- 提供图形化展示方式
- 支持简单拓扑文件持久化

## 后续规划

后续工作将围绕“从命令型路径工具升级为网络拓扑管理智能体”展开。

### 第一阶段：稳定当前智能体核心

- 将手写测试逐步迁移为 JUnit 测试
- 增加核心算法测试，包括不可达、重复边、无效权重、多条最短路径和文件加载异常
- 优化错误提示，让自然语言无法识别时给出更明确的修正建议
- 统一命令行、GUI 和未来 API 的调用入口，优先复用 `AgentCommandService`

### 第二阶段：增强拓扑分析能力

- 增加拓扑摘要能力，自动统计节点数、边数、孤立节点和不可达节点
- 增加关键节点和关键链路分析，判断某个节点或链路失效后的影响范围
- 增加瓶颈识别和优化建议，例如建议新增备份链路或降低关键链路代价
- 增加约束路径查询，例如必须经过某节点、避开某节点、限制最大跳数
- 增加 K 条最短路径算法，为用户提供更多备选路径

### 第三阶段：扩展网络指标模型

- 将单一 `weight` 扩展为更贴近网络场景的链路指标
- 支持时延、带宽、丢包率、可靠性、综合代价等字段
- 支持不同优化策略，例如最低时延、最高可靠性、最大带宽或综合最优
- 支持根据不同策略动态计算路径代价

### 第四阶段：完善可视化和交互

- 优化 Swing 界面布局和操作流程
- 让 GUI 也支持自然语言输入和解释结果展示
- 在图形界面中展示拓扑摘要、关键节点、瓶颈链路和优化建议
- 增加示例拓扑文件和运行截图，方便演示和答辩

### 第五阶段：预留大模型接入

- 抽象结构化意图对象，例如 `Intent`
- 让自然语言解析结果从字符串命令升级为结构化意图
- 预留 LLM 接入位置，让大模型负责理解复杂请求，Java 负责校验和执行
- 保持核心算法可控，避免把路径计算完全交给大模型

## 最近下一步实际操作

建议近期按以下顺序推进：

1. 引入 JUnit 测试框架，替换当前 `AgentTestRunner`
2. 为 `PathOptimizerAgent` 补充核心算法测试
3. 抽象 `Intent` 类，让自然语言解析结果不再只依赖字符串命令
4. 增加 `topology` 或 `summary` 命令，输出当前拓扑摘要
5. 增加 `analyze` 命令，初步分析孤立节点、不可达节点和关键节点
6. 让 GUI 调用 `AgentCommandService`，逐步统一命令行和图形界面的智能体入口
