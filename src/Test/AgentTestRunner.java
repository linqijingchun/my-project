package Test;

class AgentTestRunner {
    public static void main(String[] args) {
        testIntentParser();
        testAgentCommandService();
        testExplainWithoutPath();
        System.out.println("所有测试通过");
    }

    private static void testIntentParser() {
        IntentParser parser = new IntentParser();

        assertEquals("add B D 1", parser.parse("添加B到D长度为1"));
        assertEquals("addud A B 3", parser.parse("添加无向边A到B长度为3"));
        assertEquals("path A D", parser.parse("帮我查询A到D的最短路径"));
        assertEquals("allpaths A D", parser.parse("查询A到D的所有最短路径"));
        assertEquals("reach A D", parser.parse("A到D能不能到"));
        assertEquals("update A B 4", parser.parse("把A到B的权重改成4"));
        assertEquals("update A B 4", parser.parse("修改A到B长度为4"));
        assertEquals("update A B 4", parser.parse("将A到B的距离设为4"));
        assertEquals("remove A B", parser.parse("删除A到B的边"));
        assertEquals("show", parser.parse("显示当前拓扑"));
        assertEquals("explain", parser.parse("why"));
        assertEquals("explain", parser.parse("为什么"));
        assertEquals("explain", parser.parse("解释一下"));
    }

    private static void testAgentCommandService() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        AgentCommandService service = new AgentCommandService(agent);

        AgentResponse r1 = service.handle("添加A到B长度为1");
        assertTrue(r1.isSuccess(), "添加 A 到 B 应该成功");

        AgentResponse r2 = service.handle("添加B到D长度为6");
        assertTrue(r2.isSuccess(), "添加 B 到 D 应该成功");

        AgentResponse r3 = service.handle("帮我查询A到D的最短路径");
        assertTrue(r3.isSuccess(), "查询 A 到 D 最短路径应该成功");
        assertEquals("帮我查询A到D的最短路径", r3.getOriginalInput());
        assertEquals("path A D", r3.getNormalizedCommand());
        assertContains(r3.getMessage(), "A -> B -> D");
        assertContains(r3.getMessage(), "总代价: 7");

        AgentResponse r4 = service.handle("A到D能不能到");
        assertTrue(r4.isSuccess(), "A 到 D 应该可达");
        assertEquals("reach A D", r4.getNormalizedCommand());
        assertContains(r4.getMessage(), "可达");

        AgentResponse r5 = service.handle("显示当前拓扑");
        assertTrue(r5.isSuccess(), "显示拓扑应该成功");
        assertEquals("show", r5.getNormalizedCommand());
        assertContains(r5.getMessage(), "A -->");
        assertContains(r5.getMessage(), "B(1)");

        AgentResponse r6 = service.handle("path A D");
        assertTrue(r6.isSuccess(), "标准命令查询 A 到 D 应该成功");
        assertEquals("path A D", r6.getOriginalInput());
        assertEquals("path A D", r6.getNormalizedCommand());
        assertTrue(service.getContext().hasLastPath(), "context should remember the latest path query");
        assertEquals("A", service.getContext().getLastSource());
        assertEquals("D", service.getContext().getLastTarget());
        assertEquals("path A D", service.getContext().getLastNormalizedCommand());

        AgentResponse r7 = service.handle("why");
        assertTrue(r7.isSuccess(), "why should explain the latest path query");
        assertEquals("explain", r7.getNormalizedCommand());
        assertContains(r7.getMessage(), "A");
        assertContains(r7.getMessage(), "D");
        assertContains(r7.getMessage(), "A -> B -> D");
    }

    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError(
                    "期望: " + expected + "\n实际: " + actual
            );
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertContains(String actual, String expectedPart) {
        if (actual == null || !actual.contains(expectedPart)) {
            throw new AssertionError(
                    "期望内容包含: " + expectedPart + "\n实际内容: " + actual
            );
        }
    }

    private static void testExplainWithoutPath() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        AgentCommandService service = new AgentCommandService(agent);

        AgentResponse response = service.handle("why");

        assertTrue(!response.isSuccess(), "why should fail when no path has been queried");
        assertEquals("explain", response.getNormalizedCommand());
    }
}
