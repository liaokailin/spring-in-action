package com.lkl.ai.rag.etl.transformer;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.transformer.SummaryMetadataEnricher;
import org.springframework.ai.document.MetadataMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

//@Configuration
public class SummaryEnricherConfig {

    // 配置 SummaryMetadataEnricher Bean
    @Bean
    public SummaryMetadataEnricher summaryEnricher(ChatModel chatModel) {

        String template = """
                 请基于以下文本提取核心信息：
                            {context_str}
                
                要求：
                     1. 使用简体中文
                     2. 包含关键实体
                     3. 不超过50字
                
                 摘要：
                """;
        return new SummaryMetadataEnricher(chatModel
                , List.of(SummaryMetadataEnricher.SummaryType.PREVIOUS, SummaryMetadataEnricher.SummaryType.CURRENT, SummaryMetadataEnricher.SummaryType.NEXT)
                , template, MetadataMode.ALL);
    }
}