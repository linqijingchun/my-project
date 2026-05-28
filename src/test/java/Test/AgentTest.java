package Test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AgentTest {

    @Test
    void testIntentParserAdd() {
        IntentParser parser = new IntentParser();
        assertEquals("add B D 1", parser.parse("添加B到D长度为1"));
        assertEquals("addud A B 3", parser.parse("添加无向边A到B长度为3"));
    }

    @Test
    void testIntentParserQuery() {
        IntentParser parser = new IntentParser();
        assertEquals("path A D", parser.parse("帮我查询A到D的最短路径"));
        assertEquals("allpaths A D", parser.parse("查询A到D的所有最短路径"));
        assertEquals("reach A D", parser.parse("A到D能不能到"));
    }

    @Test
    void testIntentParserModify() {
        IntentParser parser = new IntentParser();
        assertEquals("update A B 4", parser.parse("把A到B的权重改成4"));
        assertEquals("remove A B", parser.parse("删除A到B的边"));
        assertEquals("show", parser.parse("显示当前拓扑"));
    }

    @Test
    void testIntentParserExplain() {
        IntentParser parser = new IntentParser();
        assertEquals("explain", parser.parse("why"));
        assertEquals("explain", parser.parse("为什么"));
        assertEquals("explain", parser.parse("解释一下"));
    }

    @Test
    void testAgentCommandServiceFullPath() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        AgentCommandService service = new AgentCommandService(agent);

        AgentResponse r1 = service.handle("添加A到B长度为1");
        assertTrue(r1.isSuccess(), "添加 A 到 B 应该成功");

        AgentResponse r2 = service.handle("添加B到D长度为6");
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

        service.handle("添加A到B长度为1");
        service.handle("添加B到D长度为6");

        AgentResponse r = service.handle("A到D能不能到");
        assertTrue(r.isSuccess());
        assertEquals("reach A D", r.getNormalizedCommand());
        assertTrue(r.getMessage().contains("可达"));
    }

    @Test
    void testShowTopology() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        AgentCommandService service = new AgentCommandService(agent);

        service.handle("添加A到B长度为1");

        AgentResponse r = service.handle("显示当前拓扑");
        assertTrue(r.isSuccess());
        assertEquals("show", r.getNormalizedCommand());
        assertTrue(r.getMessage().contains("A -->"));
        assertTrue(r.getMessage().contains("B(1)"));
    }

    @Test
    void testContextMemory() {
        PathOptimizerAgent agent = new PathOptimizerAgent();
        AgentCommandService service = new AgentCommandService(agent);

        service.handle("添加A到B长度为1");
        service.handle("添加B到D长度为6");
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

        service.handle("添加A到B长度为1");
        service.handle("添加B到D长度为6");
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
}
