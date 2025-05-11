
package com.lkl.ai.agent.workflow;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

public class RoutingWorkflow {

    private final ChatClient chatClient;

    public RoutingWorkflow(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String route(String input, Map<String, String> routes) {
        Assert.notNull(input, "Input text cannot be null");
        Assert.notEmpty(routes, "Routes map cannot be null or empty");

        String routeKey = determineRoute(input, routes.keySet());

        String selectedPrompt = routes.get(routeKey);

        if (selectedPrompt == null) {
            throw new IllegalArgumentException("Selected route '" + routeKey + "' not found in routes map");
        }

        return chatClient.prompt(selectedPrompt + "\nInput: " + input).call().content();
    }

    private String determineRoute(String input, Iterable<String> availableRoutes) {
        System.out.println("\n可用的路由: " + availableRoutes);

        String selectorPrompt = String.format("""
                分析输入内容并从以下选项中选择最合适的支持团队：%s
                         首先解释你的判断依据，然后按照以下JSON格式提供选择：
                
                         \\{
                             "reasoning": "简要说明为何该工单应分配给特定团队。
                                         需考虑关键词、用户意图和紧急程度。",
                             "selection": "所选团队名称"
                         \\}
                
                         输入：%s""", availableRoutes, input);

        RoutingResponse routingResponse = chatClient.prompt(selectorPrompt).call().entity(RoutingResponse.class);

        System.out.println(String.format("路由分析:%s\n选择结果: %s",
                routingResponse.reasoning(), routingResponse.selection()));

        return routingResponse.selection();
    }
}
