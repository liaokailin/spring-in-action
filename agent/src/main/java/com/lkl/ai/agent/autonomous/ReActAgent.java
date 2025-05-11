package com.lkl.ai.agent.autonomous;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.tool.DefaultToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ReActAgent {

    public static final String DEFAULT_SYSTEM_PROMPT = """
            Answer the following questions as best you can. You have access to the following tools:
            
                       {tools}
            
                       Use the following format:
            
                       Question: the input question you must answer
                       Thought: you should always think about what to do
                       Action: the action to take, should be one of [{toolNames}]
                       Action Input: the input to the action
                       Observation: the result of the action
                       ... (this Thought/Action/Action Input/Observation can repeat N times)
                       Thought: I now know the final answer
                       Final Answer: the final answer to the original input question
            
                       During the process of answering questions, you need to follow the rules below:
                       1. In the "Action" line, only include the name of the tool used, without any other characters.
                       2. Do not guess the answer, if you need to use an Action, wait for the user to provide the results of the Action as the next step's Observation. And do not provide the subsequent Thought and Final Answer.
            
                       Begin!
            """;


    private ChatClient chatClient;
    private ToolCallingManager toolCallingManager;
    private ToolCallbackResolver toolCallbackResolver;
    private Set<String> toolBeanNames;
    private List<ToolDefinition> toolDefinitionList;

    private Map<String, ToolCallback> toolCallbackMap;

    private String toolNames;

    private String toolDesc;


    public ReActAgent(String name, Set<String> toolBeanNames, ChatClient chatClient, ToolCallingManager toolCallingManager, ToolCallbackResolver toolCallbackResolver) {

        this.chatClient = chatClient;
        this.toolCallingManager = toolCallingManager;
        this.toolCallbackResolver = toolCallbackResolver;
        this.toolBeanNames = toolBeanNames;

        //解析可调用的工具信息
        parseTool(toolBeanNames, toolCallingManager, toolCallbackResolver);


    }


    public String call(String prompt) {


        SystemMessage systemMessage = new SystemMessage(new PromptTemplate(DEFAULT_SYSTEM_PROMPT).render(Map.of("tools", this.toolDesc, "toolNames", this.toolNames)));

        List<String> input = new ArrayList<>();
        input.add("Question:" + prompt);

        return doCall(systemMessage, input);
    }

    /**
     * 推理-行动的循序调用，要加一个执行的次数，防止死循环
     *
     * @param systemMessage
     * @param inputList
     * @return
     */
    private String doCall(SystemMessage systemMessage, List<String> inputList) {

        String inputText = String.join(System.lineSeparator(), inputList);

        Prompt prompt = new Prompt(List.of(systemMessage, new UserMessage(inputText)));

        String result = chatClient.prompt(prompt).call().content();

        System.out.println(String.format("调用大模型提示词：\n%s\n大模型返回结果:\n%s", prompt.getContents(), result));

        Object llmEntity = parseLlmResp(result);

        if (llmEntity instanceof ToolAction toolAction) {
            //调用工具之前，把上一次执行的结果记录下来
            inputList.add(result);

            String toolResp = toolCallbackMap.get(toolAction.name).call(toolAction.arguments);

            String observation = String.format("Observation:the result of action [%s] on input [%s] is [%s]", toolAction.name, toolAction.arguments, toolResp);

            inputList.add(observation);

            result = doCall(systemMessage, inputList);

        } else if (llmEntity instanceof FinalAnswer finalAnswer) {
            return finalAnswer.answer;
        }

        return result;
    }

    private Object parseLlmResp(String llmResp) {

        if (llmResp == null) {
            return null;
        }
        if (llmResp.contains("Final Answer:")) {
            return extractFinalAnswer(llmResp);
        } else {
            return extractToolAction(llmResp);
        }
    }


    private FinalAnswer extractFinalAnswer(String llmResp) {

        String[] splits = llmResp.split("Final Answer:", 2);

        return new FinalAnswer(splits[1].trim());
    }

    private ToolAction extractToolAction(String llmResp) {
        String action = null;
        String actionInput = null;
        Pattern pattern = Pattern.compile("(?i)^Action:\\s*(.+)$" + "|^Action Input:\\s*(.+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(llmResp);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                action = matcher.group(1).trim();
            } else if (matcher.group(2) != null) {
                actionInput = matcher.group(2).trim();
            }
        }
        return new ToolAction(action, actionInput);
    }

    public record ToolAction(String name, String arguments) {
    }

    public record FinalAnswer(String answer) {
    }

    private void parseTool(Set<String> toolBeanNames, ToolCallingManager toolCallingManager, ToolCallbackResolver toolCallbackResolver) {

        ToolCallingChatOptions toolCallingChatOptions = new DefaultToolCallingChatOptions();
        toolCallingChatOptions.setToolNames(toolBeanNames);

        List<ToolDefinition> toolDefinitionList = toolCallingManager.resolveToolDefinitions(toolCallingChatOptions);

        this.toolDefinitionList = toolDefinitionList;


        this.toolNames = buildToolNames(toolDefinitionList);

        this.toolDesc = buildToolDesc(toolDefinitionList);

        buildToolDefinitionMap(toolDefinitionList, toolCallbackResolver);


    }


    private void buildToolDefinitionMap(List<ToolDefinition> toolDefinitionList, ToolCallbackResolver toolCallbackResolver) {
        Map<String, ToolCallback> toolCallbackMap = new HashMap<>();
        if (CollectionUtils.isEmpty(toolDefinitionList)) {
            return;
        }
        for (ToolDefinition toolDefinition : toolDefinitionList) {

            ToolCallback toolCallback = toolCallbackResolver.resolve(toolDefinition.name());
            toolCallbackMap.put(toolDefinition.name(), toolCallback);
        }
        this.toolCallbackMap = toolCallbackMap;
    }

    private String buildToolDesc(List<ToolDefinition> toolDefinitionList) {
        if (CollectionUtils.isEmpty(toolDefinitionList)) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (ToolDefinition toolDefinition : toolDefinitionList) {
            sb.append(toolDefinition.name()).append(",");

            String desc = String.format("Tool name :%s,description:%s,input type schema:%s", toolDefinition.name(), toolDefinition.description(), toolDefinition.inputSchema());
            sb.append(desc);
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * 构建工具名称
     *
     * @param toolDefinitionList
     * @return
     */
    private String buildToolNames(List<ToolDefinition> toolDefinitionList) {

        if (CollectionUtils.isEmpty(toolDefinitionList)) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (ToolDefinition toolDefinition : toolDefinitionList) {
            sb.append(toolDefinition.name()).append(",");
        }

        return sb.toString().substring(0, sb.length() - 1);
    }
}
