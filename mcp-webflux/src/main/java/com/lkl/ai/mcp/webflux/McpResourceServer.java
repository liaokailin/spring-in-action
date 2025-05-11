package com.lkl.ai.mcp.webflux;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class McpResourceServer {

    // 静态资源：产品手册
    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> staticResources() {
        // 1. 创建资源定义（符合最新API）
        McpSchema.Resource productManual = new McpSchema.Resource("/resources/product-manual",  // URI
                "产品功能约束",         // 名称
                "详细描述天气查询工具的限制", // 描述
                "text/html",                  // MIME类型
                new McpSchema.Annotations(List.of(McpSchema.Role.USER, McpSchema.Role.ASSISTANT), // 允许访问的角色
                        0.8                            // 优先级（0.0-1.0）
                ));

        // 2. 定义资源内容处理器
        McpServerFeatures.SyncResourceSpecification spec = new McpServerFeatures.SyncResourceSpecification(productManual, (exchange, request) -> {
            try {
                String htmlContent = """
                        <html>
                            <body>
                                <h1>产品功能约束</h1>
                               * 每个用户每天最多调用10次，超过10次则会收费
                               * 目前所有的数据都是mock的，可靠度为0
                            </body>
                        </html>
                        """;

                return new McpSchema.ReadResourceResult(List.of(new McpSchema.TextResourceContents(request.uri(), "text/html", htmlContent)));
            } catch (Exception e) {
                throw new RuntimeException("Failed to load resource", e);
            }
        });

        return List.of(spec);
    }

    // 动态资源：实时系统指标
    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> dynamicResources(ObjectMapper objectMapper) {

        McpSchema.Resource systemMetrics = new McpSchema.Resource("/monitoring/system-metrics", "System Metrics", "Real-time CPU/Memory/Disk metrics", "application/json", new McpSchema.Annotations(List.of(McpSchema.Role.USER), 0.9                  // 高优先级
        ));

        McpServerFeatures.SyncResourceSpecification spec = new McpServerFeatures.SyncResourceSpecification(systemMetrics, (exchange, request) -> {
            try {
                Map<String, Object> metrics = Map.of("cpu", Map.of("usage", Math.random() * 100, "cores", Runtime.getRuntime().availableProcessors()), "memory", Map.of("free", Runtime.getRuntime().freeMemory(), "max", Runtime.getRuntime().maxMemory()), "timestamp", System.currentTimeMillis());

                return new McpSchema.ReadResourceResult(List.of(new McpSchema.TextResourceContents(request.uri(), "application/json", objectMapper.writeValueAsString(metrics))));
            } catch (Exception e) {
                throw new RuntimeException("Metrics collection failed", e);
            }
        });

        return List.of(spec);
    }

}