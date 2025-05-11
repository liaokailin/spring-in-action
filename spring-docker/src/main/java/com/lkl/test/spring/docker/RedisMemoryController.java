package com.lkl.test.spring.docker;

import com.lkl.test.spring.docker.redis.RedisChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class RedisMemoryController {

    private ChatClient chatClient;

    private RedisChatMemory redisChatMemory;

    public RedisMemoryController(ChatClient.Builder builder, RedisTemplate<String, String> redisTemplate) {

        this.redisChatMemory = new RedisChatMemory(redisTemplate);

        this.chatClient = builder.defaultAdvisors(new MessageChatMemoryAdvisor(redisChatMemory)).build();

    }

    @GetMapping("/memory/redis")
    public String chat(@RequestParam(value = "input", defaultValue = "讲一个笑话") String input) {
        Prompt prompt = new Prompt(input);
        return chatClient.prompt(prompt).call().content();
    }

}
