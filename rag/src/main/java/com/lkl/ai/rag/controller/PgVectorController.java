package com.lkl.ai.rag.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class PgVectorController {

    private ChatClient chatClient;

    @Value("classpath:/doc/菜鸟驿站站点合作协议.pdf")
    private org.springframework.core.io.Resource doc;


    public PgVectorController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Resource
    private PgVectorStore vectorStore;


    @GetMapping("/pg/init")
    public String initData() {
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder().withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder().withNumberOfBottomTextLinesToDelete(0).withNumberOfTopPagesToSkipBeforeDelete(0).build()).withPagesPerDocument(1).build();

        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(doc, config);

        TokenTextSplitter textSplitter = new TokenTextSplitter();

        vectorStore.accept(textSplitter.apply(pdfReader.get()));
        return "done";
    }

    @GetMapping("/pg/rag")
    public String pgRag(@RequestParam(value = "input", defaultValue = "和菜鸟合作驿站会有哪些义务") String input) {
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

    @GetMapping("/pg/rag2")
    public String pgRag2(@RequestParam(value = "input", defaultValue = "菜鸟驿站有什么作用") String input) {
        return chatClient.prompt(input).advisors(new QuestionAnswerAdvisor(vectorStore)).call().content();
    }


    private List<String> findSimilarDocuments(String message) {
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.builder().query(message).topK(3).build());
        return similarDocuments.stream().map(Document::getText).collect(Collectors.toList());
    }


}
