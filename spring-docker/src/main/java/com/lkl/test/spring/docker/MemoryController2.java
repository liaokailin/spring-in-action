package com.lkl.test.spring.docker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MemoryController2 {

    private ChatClient chatClient;

    private InMemoryChatMemory inMemoryChatMemory = new InMemoryChatMemory();

    public MemoryController2(ChatClient.Builder builder) {

        this.chatClient = builder.defaultAdvisors(new MessageChatMemoryAdvisor(inMemoryChatMemory)).build();

    }

    @GetMapping("/memory/chat2")
    public String chat(@RequestParam(value = "input", defaultValue = "讲一个笑话") String input) {
        Prompt prompt = new Prompt(input);
        return chatClient.prompt(prompt).call().content();
    }

}
