package Test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AgentTest {

    @Test
    void testIntentParserAdd() {
        IntentParser parser = new IntentParser();

        Intent i1 = parser.parse("添加B到D长度为1");
        assertEquals(IntentType.ADD_DIRECTED, i1.getType());
        assertEquals("B", i1.getSource());
        assertEquals("D", i1.getTarget());
        assertEquals(1, i1.getWeight());

        Intent i2 = parser.parse("添加无向边A到B长度为3");
        assertEquals(IntentType.ADD_UNDIRECTED, i2.getType());
        assertEquals("A", i2.getSource());
        assertEquals("B", i2.getTarget());
        assertEquals(3, i2.getWeight());
    }

    @Test
    void testIntentParserQuery() {
        IntentParser parser = new IntentParser();

        assertEquals(IntentType.PATH,
                parser.parse("帮我查询A到D的最短路径").getType());
        assertEquals(IntentType.ALLPATHS,
                parser.parse("查询A到D的所有最短路径").getType());
        assertEquals(IntentType.REACH,
                parser.parse("A到D能不能到").getType());
    }

    @Test
    void testIntentParserModify() {
        IntentParser parser = new IntentParser();

        Intent i1 = parser.parse("把A到B的权重改成4");
        assertEquals(IntentType.UPDATE, i1.getType());
        assertEquals(4, i1.getWeight());

        assertEquals(IntentType.REMOVE,
                parser.parse("删除A到B的边").getType());
        assertEquals(IntentType.SHOW,
                parser.parse("显示当前拓扑").getType());
    }

    @Test
    void testIntentParserExplain() {
        IntentParser parser = new IntentParser();

        assertEquals(IntentType.EXPLAIN, parser.parse("why").getType());
        assertEquals(IntentType.EXPLAIN, parser.parse("为什么").getType());
        assertEquals(IntentType.EXPLAIN, parser.parse("解释一下").getType());
    }

    @Test
    void testIntentParserStandardCommands() {
        IntentParser parser = new IntentParser();

        Intent i1 = parser.parse("add A B 5");
        assertEquals(IntentType.ADD_DIRECTED, i1.getType());
        assertEquals("A", i1.getSource());
        assertEquals("B", i1.getTarget());
        assertEquals(5, i1.getWeight());

        Intent i2 = parser.parse("path A D");
        assertEquals(IntentType.PATH, i2.getType());
        assertEquals("A", i2.getSource());
        assertEquals("D", i2.getTarget());

        Intent i3 = parser.parse("summary");
        assertEquals(IntentType.SUMMARY, i3.getType());
    }

    @Test
    void testAgentCommandServiceFullPath() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        AgentCommandService service = new AgentCommandService(agent);

        AgentResponse r1 = service.handle("添加A到B长度为5");
        assertTrue(r1.isSuccess(), "添加 A 到 B 应该成功");

        AgentResponse r2 = service.handle("添加B到D长度为2");
        assertTrue(r2.isSuccess(), "添加 B 到 D 应该成功");

        AgentResponse r3 = service.handle("帮我查询A到D的最短路径");
        assertTrue(r3.isSuccess());
        assertEquals("帮我查询A到D的最短路径", r3.getOriginalInput());
        assertEquals("path A D", r3.getNormalizedCommand());
        assertTrue(r3.getMessage().contains("A -> B -> D"));
        assertTrue(r3.getMessage().contains("总代价: 7"));
    }

    @Test
    void testReachability() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        AgentCommandService service = new AgentCommandService(agent);

        service.handle("添加A到B长度为5");
        service.handle("添加B到D长度为2");

        AgentResponse r = service.handle("A到D能不能到");
        assertTrue(r.isSuccess());
        assertEquals("reach A D", r.getNormalizedCommand());
        assertTrue(r.getMessage().contains("可达"));
    }

    @Test
    void testShowTopology() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        AgentCommandService service = new AgentCommandService(agent);

        service.handle("添加A到B长度为5");

        AgentResponse r = service.handle("显示当前拓扑");
        assertTrue(r.isSuccess());
        assertEquals("show", r.getNormalizedCommand());
        assertTrue(r.getMessage().contains("A -->"));
        assertTrue(r.getMessage().contains("B(5)"));
    }

    @Test
    void testContextMemory() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        AgentCommandService service = new AgentCommandService(agent);

        service.handle("添加A到B长度为5");
        service.handle("添加B到D长度为2");
        service.handle("path A D");

        assertTrue(service.getContext().hasLastPath());
        assertEquals("A", service.getContext().getLastSource());
        assertEquals("D", service.getContext().getLastTarget());
        assertEquals("path A D", service.getContext().getLastNormalizedCommand());
    }

    @Test
    void testExplainAfterPath() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        AgentCommandService service = new AgentCommandService(agent);

        service.handle("添加A到B长度为5");
        service.handle("添加B到D长度为2");
        service.handle("path A D");

        AgentResponse r = service.handle("why");
        assertTrue(r.isSuccess());
        assertEquals("explain", r.getNormalizedCommand());
        assertTrue(r.getMessage().contains("A"));
        assertTrue(r.getMessage().contains("D"));
        assertTrue(r.getMessage().contains("A -> B -> D"));
    }

    @Test
    void testExplainWithoutPathShouldFail() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        AgentCommandService service = new AgentCommandService(agent);

        AgentResponse response = service.handle("why");
        assertFalse(response.isSuccess(), "没有查询过路径时 why 应该失败");
        assertEquals("explain", response.getNormalizedCommand());
    }

    @Test
    void testTopologySummaryCommand() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        AgentCommandService service = new AgentCommandService(agent);

        service.handle("添加A到B长度为5");
        service.handle("添加B到D长度为2");

        AgentResponse response = service.handle("显示网络摘要");
        assertTrue(response.isSuccess());
        assertEquals("summary", response.getNormalizedCommand());
        assertTrue(response.getMessage().contains("节点数: 3"));
        assertTrue(response.getMessage().contains("边数: 2"));
    }
}