package com.lkl.test.spring.docker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
public class MemoryController {

    private ChatClient chatClient;

    public MemoryController(ChatClient.Builder builder) {

        this.chatClient = builder.build();

    }


    private InMemoryChatMemory inMemoryChatMemory = new InMemoryChatMemory();



    @GetMapping("/memory/jdbc")
    public String chatJdbc(@RequestParam(value = "input", defaultValue = "讲一个笑话") String input) {
        Prompt prompt = new Prompt(input);
        return chatClient.prompt(prompt).advisors(new MessageChatMemoryAdvisor(inMemoryChatMemory)).call().content();
    }



    @GetMapping("/memory/chat")
    public String chat(@RequestParam(value = "input", defaultValue = "讲一个笑话") String input) {
        Prompt prompt = new Prompt(input);
        return chatClient.prompt(prompt).advisors(new MessageChatMemoryAdvisor(inMemoryChatMemory)).call().content();
    }


    @GetMapping("/memory/user/chat")
    public String chatByUser(@RequestParam(value = "input", defaultValue = "讲一个笑话") String input, @RequestParam(value = "userId", defaultValue = "123456") String userId) {
        Prompt prompt = new Prompt(input);
        return chatClient.prompt(prompt).advisors(new MessageChatMemoryAdvisor(inMemoryChatMemory, userId, AbstractChatMemoryAdvisor.DEFAULT_CHAT_MEMORY_RESPONSE_SIZE)).call().content();
    }


}
