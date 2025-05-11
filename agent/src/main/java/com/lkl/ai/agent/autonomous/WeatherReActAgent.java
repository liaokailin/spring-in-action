package com.lkl.ai.agent.autonomous;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class WeatherReActAgent extends ReActAgent {


    public WeatherReActAgent(ChatClient.Builder builder, ToolCallingManager toolCallingManager, ToolCallbackResolver toolCallbackResolver) {
        super("weatherReActAgent", Set.of("currentWeather"), builder.build(), toolCallingManager, toolCallbackResolver);
    }
}
