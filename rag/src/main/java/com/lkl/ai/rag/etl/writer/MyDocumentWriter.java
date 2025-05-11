package com.lkl.ai.rag.etl.writer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.writer.FileDocumentWriter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
//@Component
public class MyDocumentWriter {

    public void writeDocuments(List<Document> documents) {
        FileDocumentWriter writer = new FileDocumentWriter("output.txt", true, MetadataMode.ALL, false);
        writer.accept(documents);
    }


    @Bean
    public CommandLineRunner documentWriterCommandLineRunner() {
        return args -> {
            // 创建测试文档
            Document doc1 = new Document("春天来了，大地复苏。树木抽出新芽，花朵竞相开放。");
            Document doc2 = new Document("夏季是万物生长的季节。阳光充足，植物茂盛，动物活跃。");
            Document doc3 = new Document("秋天是收获的季节。果实成熟，树叶变黄，天气凉爽。");
           writeDocuments(List.of(doc1, doc2, doc3));


        };
    }

}