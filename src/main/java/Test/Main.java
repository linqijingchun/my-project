package Test;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        AgentCommandService commandService = new AgentCommandService(agent);
        Scanner scanner = new Scanner(System.in);

        printHelp();

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();

            if (line.equalsIgnoreCase("exit")) {
                break;
            }

            AgentResponse response = commandService.handle(line);
            if (response.getNormalizedCommand() != null
                    && !response.getNormalizedCommand().equals(response.getOriginalInput())) {
                System.out.println("识别为命令: " + response.getNormalizedCommand());
            }
            System.out.println(response.getMessage());
        }

        scanner.close();
        System.out.println("智能体已退出");
    }

    private static void printHelp() {
        System.out.println("欢迎使用网络路径寻优智能体");
        System.out.println("命令列表：");
        System.out.println("  add <from> <to> <weight>        - 添加有向边");
        System.out.println("  addud <from> <to> <weight>      - 添加无向边");
        System.out.println("  path <src> <dst>                - 查询最短路径");
        System.out.println("  reach <src> <dst>               - 判断是否可达");
        System.out.println("  update <from> <to> <weight>     - 修改边的权重");
        System.out.println("  remove <from> <to>              - 删除一条边");
        System.out.println("  show                            - 显示当前图");
        System.out.println("  load <filename>                 - 从文件加载拓扑");
        System.out.println("  save <filename>                 - 保存拓扑到文件");
        System.out.println("  allpaths <src> <dst>            - 查询所有最短路径");
        System.out.println("  explain                         - 解释最近一次路径查询");
        System.out.println("  help                            - 查看使用指南");
        System.out.println("  summary                         - 输出拓扑摘要");
        System.out.println("  topology                        - 输出拓扑摘要");
        System.out.println("  exit                            - 退出程序");
    }
}
