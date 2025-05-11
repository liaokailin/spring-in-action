package com.lkl.ai.rag.etl.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
//@Component
class MyTextReader {

    private final Resource resource;

    MyTextReader(@Value("classpath:student.txt") Resource resource) {
        this.resource = resource;
    }

    List<Document> loadText() {
        TextReader textReader = new TextReader(this.resource);
    //    textReader.getCustomMetadata().put("filename", "text-source.txt");

    //    List<Document> splitDocuments = new TokenTextSplitter().apply(textReader.read());
        return textReader.read();
    }

    @Bean
    public CommandLineRunner textReaderCommandLineRunner() {
        return args -> {

            List<Document> documentList = this.loadText();
            log.info("MyTextReader {} documents loaded", documentList.size());
            for (Document document : documentList) {
                log.info("MyTextReader document: {}", document);
            }
        };
    }
}