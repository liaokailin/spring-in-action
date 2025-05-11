package com.lkl.ai.rag.etl.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
//@Component
public class MyTikaDocumentReader {

    private final Resource resource;

    public MyTikaDocumentReader(@Value("classpath:/student.docx") Resource resource) {
        this.resource = resource;
    }

    public List<Document> loadText() {
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        return reader.read();
    }

    @Bean
    public CommandLineRunner tikaCommandLineRunner() {
        return args -> {

            List<Document> documentList = this.loadText();
            log.info("MyTikaDocumentReader {} documents loaded", documentList.size());
            for (Document document : documentList) {
                log.info("MyTikaDocumentReader document: {}", document);
            }
        };
    }
}