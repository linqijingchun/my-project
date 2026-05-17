# 网络路径寻优智能体 1.1

这是一个基于 Java SE 的网络路径寻优项目，当前版本提供命令行和 Swing 图形界面两种使用方式。项目使用有向加权图表示网络拓扑，并通过 Dijkstra 算法计算节点之间的最短路径。

从 1.1 版本开始，项目加入了智能体式指令层和基础自然语言解析能力。用户既可以输入标准命令，也可以输入常见自然语言描述，例如“添加B到D长度为1”或“帮我查询A到D的最短路径”。

## 功能特性

- 支持添加有向边和无向边
- 支持修改、删除网络边
- 支持查询两点之间的最短路径
- 支持判断两个节点是否可达
- 支持查询多条等价最短路径
- 支持从文本文件加载拓扑
- 支持将当前拓扑保存到文本文件
- 提供 Swing 图形界面展示节点、边、权重和高亮路径
- 支持统一响应对象 `AgentResponse`
- 支持智能体指令调度 `AgentCommandService`
- 支持基础自然语言解析 `IntentParser`
- 支持 `help` 指令查看使用指南

## 项目结构

```text
netTest
├── NetTest.iml
├── README.md
└── src
    └── Test
        ├── AgentCommandService.java
        ├── AgentResponse.java
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

1.1 版本将命令行交互、指令解析、核心算法和响应结果进一步拆分：

```text
Main
读取用户输入，处理 exit，展示结果。

IntentParser
把常见自然语言转换成标准命令。

AgentCommandService
解析标准命令，调度 PathOptimizerAgent，并包装 AgentResponse。

PathOptimizerAgent
维护拓扑结构，执行路径寻优、可达性判断、边操作和文件读写。

AgentResponse
统一返回执行结果，方便命令行、GUI 或未来 API 复用。
```

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

- 增加单元测试
- 增加自然语言解析测试样例
- 优化自然语言解析边界和错误提示
- 增加 K 条最短路径算法
- 支持更多网络指标，例如带宽、时延、丢包率、可靠性
- 优化 Swing 界面布局和交互体验
- 增加示例拓扑文件和使用截图
- 预留 LLM 接入，让大模型输出结构化意图或标准命令
