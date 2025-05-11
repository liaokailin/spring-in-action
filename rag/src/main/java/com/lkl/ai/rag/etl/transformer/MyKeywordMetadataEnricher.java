package com.lkl.ai.rag.etl.transformer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.transformer.KeywordMetadataEnricher;
import org.springframework.ai.document.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
//@Component
public class MyKeywordMetadataEnricher {

    private final ChatModel chatModel;

    MyKeywordMetadataEnricher(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(this.chatModel, 5);
        return enricher.apply(documents);
    }

    @Bean
    public CommandLineRunner keywordMetadataCommandLineRunner() {
        return args -> {
            Document doc = new Document("""
                    春天来了，大地披上绿装。和煦的阳光洒满田野，嫩绿的草芽从泥土中探出头来，枝头的花朵竞相绽放，空气中弥漫着淡淡的花香。
                    微风轻拂，带来泥土的芬芳和鸟儿的欢唱。
                    孩子们在草地上奔跑，风筝在蓝天中飞舞，一切都充满生机与希望。
                    春天是生命的季节，是梦想的开始，让人心旷神怡，充满期待。
                    """);

            List<Document> documentList = enrichDocuments(List.of(doc));

            String keywords = (String) documentList.get(0).getMetadata().get("excerpt_keywords");

            System.out.println("提取的关键词: " + keywords);
        };
    }

}  