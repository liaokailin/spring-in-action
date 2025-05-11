package com.lkl.test.spring.docker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HelloController {

    private ChatClient chatClient;

    public HelloController(ChatClient.Builder builder) {

        this.chatClient = builder.build();
    }


    @GetMapping("/log")
    public String log(@RequestParam(value = "input", defaultValue = "讲一个笑话") String input) {
        Prompt prompt = new Prompt(input);
        return chatClient.prompt(prompt).advisors(new SimpleLoggerAdvisor()).call().content();
    }


    @GetMapping("/prompt/simple")
    public String simplePrompt(@RequestParam(value = "input", defaultValue = "讲一个笑话") String input) {
        Prompt prompt = new Prompt(input);
        return chatClient.prompt(prompt).call().content();
    }


}
