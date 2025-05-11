package com.lkl.ai.rag.etl.transformer;

import org.springframework.ai.document.DefaultContentFormatter;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.transformer.ContentFormatTransformer;

import java.util.List;
import java.util.Map;

public class MyContentFormatTransformer {

    public static void main(String[] args) {

        Document doc = new Document(
                "Spring AI 最新版本发布",
                Map.of(
                        "author", "Spring AI",
                        "date", "2025-05",
                        "internal_id", "X-123",
                        "timestamp", "122323232"
                )
        );

        DefaultContentFormatter formatter = DefaultContentFormatter.builder()
                .withMetadataTemplate("{key}>>>{value}")  // 元数据显示格式
                .withMetadataSeparator("\n")             // 元数据分隔符
                .withTextTemplate("METADATA:\n{metadata_string}\nCONTENT:\n{content}") // 内容模板
                .withExcludedInferenceMetadataKeys("internal_id")  // 推理时排除的元数据
                .withExcludedEmbedMetadataKeys("timestamp")       // 嵌入时排除的元数据
                .build();

        String content = formatter.format(doc, MetadataMode.EMBED);
        System.out.println(content);

        ContentFormatTransformer transformer = new ContentFormatTransformer(formatter, false);

        List<Document> ll = transformer.apply(List.of(doc));


        System.out.println(ll.size());
        for (Document document : ll) {
            System.out.println(document);
        }

    }
}
