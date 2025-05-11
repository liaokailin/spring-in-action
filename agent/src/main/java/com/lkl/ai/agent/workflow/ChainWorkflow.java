package com.lkl.ai.agent.workflow;

import org.springframework.ai.chat.client.ChatClient;

public class ChainWorkflow {


    private static final String[] DEFAULT_SYSTEM_PROMPTS = {

            // 步骤1
            """
            从文本中仅提取数值及其关联指标。
            每条数据格式为"数值: 指标"，各占一行。
            示例格式：
            92: 客户满意度
            45%: 收入增长率""",

            // 步骤2
            """
            将所有数值尽可能转换为百分比形式。
            若非百分比或点数，则转换为小数（如92点 -> 92%）。
            保持每行一个数值。
            示例格式：
            92%: 客户满意度
            45%: 收入增长率""",

            // 步骤3
            """
            按数值降序排列所有行。
            保持每行"数值: 指标"的格式。
            示例：
            92%: 客户满意度
            87%: 员工满意度""",

            // 步骤4
            """
            将排序后的数据格式化为Markdown表格，包含列：
            | 指标 | 数值 |
            |:--|--:|
            | 客户满意度 | 92% | """};
    private final ChatClient chatClient;

    private final String[] systemPrompts;


    public ChainWorkflow(ChatClient chatClient) {
        this(chatClient, DEFAULT_SYSTEM_PROMPTS);
    }


    public ChainWorkflow(ChatClient chatClient, String[] systemPrompts) {
        this.chatClient = chatClient;
        this.systemPrompts = systemPrompts;
    }

    public String chain(String userInput) {

        int step = 0;
        String response = userInput;
        System.out.println(String.format("\nSTEP %s:\n %s", step++, response));

        for (String prompt : systemPrompts) {

            // 1. Compose the input using the response from the previous step.
            String input = String.format("{%s}\n {%s}", prompt, response);

            // 2. Call the chat client with the new input and get the new response.
            response = chatClient.prompt(input).call().content();

            System.out.println(String.format("\nSTEP %s:\n %s", step++, response));
        }

        return response;
    }
}
