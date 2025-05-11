package com.lkl.ai.agent;

import com.lkl.ai.agent.autonomous.PlanningAgent;
import com.lkl.ai.agent.autonomous.ReflectionAgent;
import com.lkl.ai.agent.autonomous.WeatherReActAgent;
import com.lkl.ai.agent.workflow.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class HelloController {

    private ChatClient chatClient;

    public HelloController(ChatClient.Builder builder) {

        this.chatClient = builder.build();
    }


    @GetMapping("/hi")
    public String hi(@RequestParam(value = "input", defaultValue = "讲一个笑话") String input) {
        return chatClient.prompt(input).call().content();
    }

    @GetMapping("/chain")
    public String chain() {

        String report = """
                Q3 业绩摘要：
                本季度客户满意度得分提升至 92 分。
                与去年同期相比，收入增长 45%。
                我们在主要市场的份额现已达到 23%。
                客户流失率从 8% 降至 5%。
                新用户获客成本为 43 元/用户。
                产品采用率提升至 78%。
                员工满意度得分为 87 分。
                营业利润率提升至 34%。
                """;

        return new ChainWorkflow(chatClient).chain(report);
    }


    @GetMapping("/routing")
    public void routing() {

        Map<String, String> supportRoutes = Map.of("billing", """
                        您是一位账单支持专家。请遵循以下准则：
                        1. 始终以"账单支持回复："开头
                        2. 首先确认具体的账单问题
                        3. 清晰地解释任何费用或差异
                        4. 列出具体的后续步骤及时间安排
                        5. 如适用，最后提供支付选项
                        
                        保持回复专业且友好。
                        
                        输入： """,

                "technical", """
                        您是一位技术支持工程师。请遵循以下准则：
                        1. 始终以"技术支持回复："开头
                        2. 列出解决问题的具体步骤
                        3. 如适用，包含系统要求
                        4. 提供常见问题的临时解决方案
                        5. 如需升级处理，说明升级路径
                        
                        使用清晰的编号步骤和技术细节。
                        
                        输入： """,

                "account", """
                        您是一位账户安全专家。请遵循以下准则：
                        1. 始终以"账户支持回复："开头
                        2. 优先处理账户安全和验证
                        3. 提供账户恢复/变更的明确步骤
                        4. 包含安全提示和警告
                        5. 设定明确的解决时间预期
                        
                        保持严肃、以安全为核心的语气。
                        
                        输入： """,

                "product", """
                        您是一位产品专家。请遵循以下准则：
                        1. 始终以"产品支持回复："开头
                        2. 专注于功能教育和最佳实践
                        3. 包含具体的使用示例
                        4. 链接到相关文档章节
                        5. 建议可能有帮助的相关功能
                        
                        保持教育性和鼓励性的语气。
                        
                        输入： """);
        List<String> tickets = List.of("""
                        标题：无法登录我的账户
                        内容：您好，过去一小时我一直在尝试登录，但总是收到"密码错误"提示。
                        我确定输入的是正确密码。能否帮我恢复访问权限？这很紧急，因为我需要
                        在今天下班前提交一份报告。
                        - 张三""",


                """
                        标题：如何导出数据？
                        内容：我需要将所有项目数据导出到Excel。我查阅了文档但找不到批量导出的方法。
                        这个功能可以实现吗？如果可以，能否一步步指导我操作？
                        此致，
                        李四""");

        var routerWorkflow = new RoutingWorkflow(chatClient);

        int i = 1;
        for (String ticket : tickets) {
            System.out.println("\n工单 " + i++);
            System.out.println("------------------------------------------------------------");
            System.out.println(ticket);
            System.out.println("------------------------------------------------------------");
            System.out.println(routerWorkflow.route(ticket, supportRoutes));
        }
    }


    @GetMapping("/parallelization")
    public void parallelization() {

        List<String> parallelResponse = new ParallelizationWorkflow(chatClient).parallel("""
                分析市场变化将如何影响该利益相关方群体。
                提供具体影响和推荐行动方案。
                使用清晰的分区和优先级进行格式化。
                """, List.of("""
                        客户：
                        - 对价格敏感
                        - 期望更好的技术
                        - 关注环境问题
                        """,

                """
                        员工：
                        - 担忧工作稳定性
                        - 需要新技能
                        - 希望明确方向
                        """,

                """
                        投资者：
                        - 期望增长
                        - 要求成本控制
                        - 关注风险
                        """,

                """
                        供应商：
                        - 产能限制
                        - 价格压力
                        - 技术转型
                        """), 4);

        System.out.println(parallelResponse);
    }


    @GetMapping("/orchestrator")
    public void orchestrator() {

        new OrchestratorWorkers(chatClient).process("为一个新的环保水瓶写一篇产品描述");
    }

    @GetMapping("/evaluator")
    public void evaluator() {

        EvaluatorOptimizer.RefinedResponse refinedResponse = new EvaluatorOptimizer(chatClient).loop("""
                <user input>
                           实现一个具有以下功能的Java栈：
                                1. push(x) - 入栈操作
                                2. pop() - 出栈操作
                                3. getMin() - 获取最小值
                                所有操作的时间复杂度应为O(1)。
                                所有内部字段必须声明为private，使用时需加"this."前缀。
                </user input>
                """);

        System.out.println("最后输出结果:\n : " + refinedResponse);
    }

    @Autowired
    private PlanningAgent planningAgent;

    @GetMapping("/planning")
    public void planning() {

        String resp = planningAgent.executeGoal("杭州51适合出游吗？", 3);
        System.out.println("最终结果：" + resp);
    }

    @Autowired
    private ReflectionAgent reflectionAgent;

    @GetMapping("/reflection")
    public void reflection() {
        String resp = reflectionAgent.run("通过java实现归并排序", 3);
        System.out.println("最终结果：" + resp);
    }

    @Autowired
    private WeatherReActAgent weatherReactAgent;

    @GetMapping("/react")
    public void react() {
        String resp = weatherReactAgent.call("查杭州的天气");
        System.out.println("最终结果：" + resp);
    }


}
