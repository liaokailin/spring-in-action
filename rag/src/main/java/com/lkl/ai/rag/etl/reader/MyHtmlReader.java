package com.lkl.ai.rag.etl.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.reader.jsoup.config.JsoupDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
//@Component
class MyHtmlReader {
    private final Resource resource;

    MyHtmlReader(@Value("classpath:/student.html") Resource resource) {
        this.resource = resource;
    }

    public List<Document> loadHtml() {
        // 构建配置参数
        JsoupDocumentReaderConfig config = JsoupDocumentReaderConfig.builder()
                .selector("article p")       // 提取<article>标签内的段落
                .charset("UTF-8")      // 指定字符编码
                .includeLinkUrls(true)      // 在元数据中包含链接URL
                .metadataTags(List.of("author", "date"))  // 提取作者和日期meta标签
                .additionalMetadata("source", "student.html") // 添加自定义元数据
                .build();
        JsoupDocumentReader reader = new JsoupDocumentReader(this.resource, config);
        return reader.get();
    }

    @Bean
    public CommandLineRunner htmlReaderCommandLineRunner() {
        return args -> {

            List<Document> documentList = this.loadHtml();
            log.info("MyHtmlReader {} documents loaded", documentList.size());
            for (Document document : documentList) {
                log.info("MyHtmlReader document: {}", document);
            }
        };
    }
}