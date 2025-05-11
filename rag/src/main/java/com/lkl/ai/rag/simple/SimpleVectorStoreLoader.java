package com.lkl.ai.rag.simple;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
//@Configuration
public class SimpleVectorStoreLoader {
    @Value("classpath:/doc/菜鸟驿站站点合作协议.txt")
    private Resource faq;


    @Bean
    public SimpleVectorStore mySimpleVectorStore(EmbeddingModel embeddingModel) throws IOException {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();

        File dbFile = getSimpleVectorStoreFile("simple_db_file.json");


        if (dbFile.exists()) {
            //文件存在，直接加载数据即可
            simpleVectorStore.load(dbFile);
            return simpleVectorStore;
        }

        Files.createFile(dbFile.toPath());
        //将文件转换为Documents
        TextReader textReader = new TextReader(faq);
        textReader.getCustomMetadata().put("fileName", "菜鸟驿站站点合作协议.txt");
        List<Document> documentList = textReader.get();
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        List<Document> splitDocuments = tokenTextSplitter.apply(documentList);
        //写入db
        simpleVectorStore.add(splitDocuments);
        simpleVectorStore.save(dbFile);

        return simpleVectorStore;
    }

    /**
     * 构建DB文件，由于第一次执行时，文件不存在，因此做路径拼接
     *
     * @return
     */
    private File getSimpleVectorStoreFile(String dbFileName) {
        Path path = Paths.get("src", "main", "resources", "data");
        String absolutePath = path.toAbsolutePath() + File.separator + dbFileName;
        return new File(absolutePath);
    }

}
