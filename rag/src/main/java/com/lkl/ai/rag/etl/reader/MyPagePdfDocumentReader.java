package com.lkl.ai.rag.etl.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
//@Component
public class MyPagePdfDocumentReader {

    List<Document> getDocsFromPdf() {

        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader("classpath:/student.pdf",
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)
                                .build())
                        .withPagesPerDocument(1)
                        .build());

        return pdfReader.read();
    }

    @Bean
    public CommandLineRunner pdfCommandLineRunner() {
        return args -> {

            List<Document> documentList = this.getDocsFromPdf();
            log.info("MyPagePdfDocumentReader {} documents loaded", documentList.size());
            for (Document document : documentList) {
                log.info("MyPagePdfDocumentReader document: {}", document);
            }
        };
    }

}