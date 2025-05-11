package com.lkl.ai.rag.etl.transformer;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

import java.util.List;
import java.util.Map;

public class MyTokenTextSplitter {
    public static void main(String[] args) {
        Document doc = new Document("自然语言处理(NLP)是AI的核心领域之一。它使计算机能理解人类语言。", Map.of("source", "AI百科"));

        TokenTextSplitter splitter = new TokenTextSplitter(30,     // defaultChunkSize（目标Token数，需调小）
                20,     // minChunkSizeChars（最小字符数，覆盖中文特性）
                1,      // minChunkLengthToEmbed（避免过滤短句）
                10,     // maxNumChunks（限制分块数）
                false   // keepSeparator（中文通常无需保留换行符）
        );

        List<Document> ll = splitter.apply(List.of(doc));
        System.out.println(ll.size());
        for (Document document : ll) {
            System.out.println(document);
        }
    }
}
