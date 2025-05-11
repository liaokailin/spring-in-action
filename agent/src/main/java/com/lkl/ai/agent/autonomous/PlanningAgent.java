package com.lkl.ai.agent.autonomous;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlanningAgent {

    private final ChatClient plannerChatClient;
    private final ChatClient executorChatClient;
    private final ChatClient mockToolExecutorChatClient;

    public PlanningAgent(ChatModel chatModel) {
        this.plannerChatClient = ChatClient.builder(chatModel).defaultSystem("""
                你是一个任务规划专家，负责将用户目标拆解为可执行的子任务，最多拆解为5个任务
                输出格式为 JSON 列表，例如：["任务1", "任务2", ...]
                """).build();

        this.executorChatClient = ChatClient.builder(chatModel).defaultSystem("""
                你是一个任务执行者，根据子任务描述完成具体操作。
                如果是需要工具调用的任务（如搜索、计算），请标注为：<TOOL>工具名:参数</TOOL>。
                否则直接生成结果。
                """).build();

        this.mockToolExecutorChatClient = ChatClient.builder(chatModel).defaultSystem("""
                你是一个工具调用模拟器，基于用户调用的工具名和参数模拟一条看起来合理的数据返回。
                """).build();

    }


    /**
     * 执行规划型任务
     *
     * @param goal       用户目标（如"写一篇关于Spring AI的博客"）
     * @param maxRetries 最大重试次数（用于任务失败时重新规划）
     */
    public String executeGoal(String goal, int maxRetries) {
        List<String> plan = generatePlan(goal);
        String result = "";

        for (int i = 0; i < plan.size(); i++) {
            String task = plan.get(i);
            String taskResult = executeTask(task);

            // 检查任务结果是否需要重试
            if (taskResult.contains("<FAIL>") && maxRetries > 0) {
                System.out.println("Retrying task: " + task);
                plan = replan(goal, result); // 动态调整计划
                maxRetries--;
                i--; // 重新执行当前任务
                continue;
            }
            result += taskResult + "\n\n";
        }
        return result;
    }

    /**
     * 生成任务计划
     */
    private List<String> generatePlan(String goal) {
        String planJson = plannerChatClient.prompt(goal).call().content();
        // 简单解析JSON格式的列表（实际项目建议用Jackson/Gson）
        List<String> planList = parseJsonList(planJson);
        String planDesc = "目标：" + goal + "\n计划拆分：" + planList;
        System.out.println(planDesc);
        return planList;
    }

    /**
     * 执行单个子任务
     */
    private String executeTask(String task) {
        String response = executorChatClient.prompt(task).call().content();

        // 检查是否需要调用工具
        if (response.contains("<TOOL>")) {
            String toolCall = response.substring(response.indexOf("<TOOL>") + 6, response.indexOf("</TOOL>"));
            String[] parts = toolCall.split(":");
            String toolName = parts[0];
            String params = parts.length > 1 ? parts[1] : "";
            return callTool(toolName, params);
        }
        return response;
    }

    public String callTool(String toolName, String params) {
        return mockToolExecutorChatClient.prompt("工具名:" + toolName + ",参数:" + params).call().content();
    }

    /**
     * 动态重新规划（基于当前结果）
     */
    private List<String> replan(String goal, String currentResult) {
        String prompt = "原始目标：" + goal + "\n当前结果：" + currentResult + "\n请重新生成剩余任务计划。";
        return generatePlan(prompt);
    }

    // 简易JSON解析（示例用）
    private List<String> parseJsonList(String json) {
        List<String> tasks = new ArrayList<>();
        json = json.replace("[", "").replace("]", "").replace("\"", "");
        for (String task : json.split(",")) {
            tasks.add(task.trim());
        }
        return tasks;
    }
}