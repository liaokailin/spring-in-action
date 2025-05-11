package com.lkl.ai.rag.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
//@RestController
public class SimpleRagController {

    private ChatClient chatClient;

    @Resource
    private VectorStore mySimpleVectorStore;


    public SimpleRagController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }


    @GetMapping("/rag")
    public String rag(@RequestParam(value = "input", defaultValue = "和菜鸟合作驿站会有哪些权利") String input) {
        PromptTemplate promptTemplate = new PromptTemplate("""
                你是一个驿站站点合作协议答疑助手，请结合文档提供的内容回答用户的问题，如果不知道请直接回答不知道
                
                用户输入的问题:
                {input}
                
                文档:
                {documents}
                """);

        String relevantDocs = String.join("\n", findSimilarDocuments(input));

        log.info("用户问题={}，查询结果={}", input, relevantDocs);

        Map<String, Object> params = new HashMap<>();

        params.put("input", input);
        params.put("documents", relevantDocs);

        return chatClient.prompt(new Prompt(promptTemplate.render(params))).call().content();
    }




    private List<String> findSimilarDocuments(String message) {
        List<Document> similarDocuments = mySimpleVectorStore.similaritySearch(SearchRequest.builder().query(message).topK(3).build());
        return similarDocuments.stream().map(Document::getText).collect(Collectors.toList());
    }


}
