
package com.lkl.ai.agent.autonomous;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class ReflectionAgent {

    private final ChatClient generateChatClient;

    private final ChatClient critiqueChatClient;


    public ReflectionAgent(ChatModel chatModel) {
        this.generateChatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                你是一名Java程序员，负责生成高质量的Java代码。
                你的任务是为用户的请求生成最佳内容。如果用户提供批评意见，
                请根据反馈修改之前的尝试并重新生成。
                """)
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();

        this.critiqueChatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                你的任务是针对用户生成的内容提供批评和改进建议。
                如果用户内容有错误或需要改进之处，请输出建议列表
                和批评意见。如果用户内容没有问题，请输出：<OK>
                """)
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
    }

    public String run(String userQuestion, int maxIterations) {

        String generation = generateChatClient.prompt(userQuestion).call().content();
        System.out.println("##generation\n\n" + generation);
        String critique;
        for (int i = 0; i < maxIterations; i++) {

            critique = critiqueChatClient.prompt(generation).call().content();

            System.out.println("##Critique\n\n" + critique);
            if (critique.contains("<OK>")) {
                System.out.println("\n\nStop sequence found\n\n");
                break;
            }
            generation = generateChatClient.prompt(critique).call().content();
        }
        return generation;

    }

}
