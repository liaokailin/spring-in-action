package com.lkl.ai.rag.etl.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
//@Component
class MyJsonReader {

    private final Resource resource;

    MyJsonReader(@Value("classpath:student.json") Resource resource) {
        this.resource = resource;
    }

    public List<Document> loadJsonAsDocuments() {
        JsonReader jsonReader = new JsonReader(this.resource);
        return jsonReader.get();
    }

    @Bean
    public CommandLineRunner jsonReaderCommandLineRunner() {
        return args -> {

            List<Document> documentList = this.loadJsonAsDocuments();
            log.info("MyJsonReader {} documents loaded", documentList.size());
            for (Document document : documentList) {

                log.info("MyJsonReader document: {}", document);
            }
        };
    }
}