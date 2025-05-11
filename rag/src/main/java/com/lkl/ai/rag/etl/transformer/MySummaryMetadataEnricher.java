package com.lkl.ai.rag.etl.transformer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.transformer.SummaryMetadataEnricher;
import org.springframework.ai.document.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
//@Component
public class MySummaryMetadataEnricher {

    private final SummaryMetadataEnricher enricher;

    // 通过构造函数注入
    public MySummaryMetadataEnricher(SummaryMetadataEnricher enricher) {
        this.enricher = enricher;
    }

    // 文档增强方法
    public List<Document> enrichDocuments(List<Document> documents) {
        return enricher.apply(documents);
    }


    @Bean
    public CommandLineRunner summaryMetadataCommandLineRunner() {
        return args -> {
            // 创建测试文档
            Document doc1 = new Document("春天来了，大地复苏。树木抽出新芽，花朵竞相开放。");
            Document doc2 = new Document("夏季是万物生长的季节。阳光充足，植物茂盛，动物活跃。");
            Document doc3 = new Document("秋天是收获的季节。果实成熟，树叶变黄，天气凉爽。");
            // 应用增强器
            List<Document> enrichedDocs = enrichDocuments(List.of(doc1, doc2, doc3));

            // 打印结果
            for (int i = 0; i < enrichedDocs.size(); i++) {
                Document doc = enrichedDocs.get(i);
                System.out.println("\n文档 " + (i + 1) + " 的元数据:");
                System.out.println("当前摘要: " + doc.getMetadata().get("section_summary"));
                System.out.println("前一篇摘要: " + doc.getMetadata().get("prev_section_summary"));
                System.out.println("后一篇摘要: " + doc.getMetadata().get("next_section_summary"));
            }
        };
    }

}
