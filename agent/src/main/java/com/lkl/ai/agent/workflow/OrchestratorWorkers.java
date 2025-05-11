
package com.lkl.ai.agent.workflow;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

public class OrchestratorWorkers {

    private final ChatClient chatClient;
    private final String orchestratorPrompt;
    private final String workerPrompt;

    public static final String DEFAULT_ORCHESTRATOR_PROMPT = """
         
            分析此任务并分解为2-3种不同的处理方式：
                   
                    任务：{task}
                   
                    请按以下JSON格式返回响应：
                    \\{
                    "analysis": "说明你对任务的理解以及哪些变化会很有价值。
                                重点关注每种方法如何服务于任务的不同方面。",
                    "tasks": [
                    	\\{
                    	"type": "formal",
                    	"description": "撰写精确的技术版本，强调规格参数"
                    	\\},
                    	\\{
                    	"type": "conversational",
                    	"description": "撰写引人入胜的友好版本，与读者建立联系"
                    	\\}
                    ]
                    \\}
            """;

    public static final String DEFAULT_WORKER_PROMPT = """
             根据以下要求生成内容：
                 任务：{original_task}
                 风格：{task_type}
                 指南：{task_description}
            """;


    public static record Task(String type, String description) {
    }


    public static record OrchestratorResponse(String analysis, List<Task> tasks) {
    }


    public static record FinalResponse(String analysis, List<String> workerResponses) {
    }

    public OrchestratorWorkers(ChatClient chatClient) {
        this(chatClient, DEFAULT_ORCHESTRATOR_PROMPT, DEFAULT_WORKER_PROMPT);
    }


    public OrchestratorWorkers(ChatClient chatClient, String orchestratorPrompt, String workerPrompt) {
        Assert.notNull(chatClient, "ChatClient must not be null");
        Assert.hasText(orchestratorPrompt, "Orchestrator prompt must not be empty");
        Assert.hasText(workerPrompt, "Worker prompt must not be empty");

        this.chatClient = chatClient;
        this.orchestratorPrompt = orchestratorPrompt;
        this.workerPrompt = workerPrompt;
    }

    public FinalResponse process(String taskDescription) {
        Assert.hasText(taskDescription, "Task description must not be empty");

        // Step 1: Get orchestrator response
        OrchestratorResponse orchestratorResponse = this.chatClient.prompt()
                .user(u -> u.text(this.orchestratorPrompt)
                        .param("task", taskDescription))
                .call()
                .entity(OrchestratorResponse.class);

        System.out.println(String.format("\n=== ORCHESTRATOR OUTPUT ===\nANALYSIS: %s\n\nTASKS: %s\n",
                orchestratorResponse.analysis(), orchestratorResponse.tasks()));

        // Step 2: Process each task
        List<String> workerResponses = orchestratorResponse.tasks().stream().map(task -> this.chatClient.prompt()
                .user(u -> u.text(this.workerPrompt)
                        .param("original_task", taskDescription)
                        .param("task_type", task.type())
                        .param("task_description", task.description()))
                .call()
                .content()).toList();

        System.out.println("\n=== WORKER OUTPUT ===\n" + workerResponses);

        return new FinalResponse(orchestratorResponse.analysis(), workerResponses);
    }

}
