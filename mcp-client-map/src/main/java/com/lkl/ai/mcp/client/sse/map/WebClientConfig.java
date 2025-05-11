package com.lkl.ai.mcp.client.sse.map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder().filter((request, next) -> {
            // 拦截 SSE 请求
            if (request.url().toString().contains("/sse")) {
                // 在原始 URL 后追加 key 参数
                URI newUri = UriComponentsBuilder.fromUri(request.url())
                        .queryParam("key", "9fa1ff2d029221792191010c6d6bf0eb") // 自动处理编码
                        .build()
                        .toUri();

                // 保留原始请求头（关键！）
                ClientRequest mutatedRequest = ClientRequest.from(request)
                        .url(newUri)
                        .build();
                return next.exchange(mutatedRequest);
            }
            return next.exchange(request);
        });
    }
}
