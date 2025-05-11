package com.lkl.mcp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebFluxSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.mcp.server.autoconfigure.McpServerProperties;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebFluxMcpConfig implements WebFluxConfigurer {
  /*  @Bean
    public WebMvcSseServerTransportProvider transportProvider(ObjectMapper mapper) {
        return new WebMvcSseServerTransportProvider(mapper, "/mcp"); // 基础路径设为/mcp
    }

    @Bean
    public RouterFunction<ServerResponse> mcpRouterFunction(WebMvcSseServerTransportProvider transportProvider) {
        return transportProvider.getRouterFunction();
    }*/

   /* @Bean
    public WebFluxSseServerTransportProvider transportProvider(ObjectMapper mapper) {

        return new WebFluxSseServerTransportProvider(mapper, "/mcp");
    }*/

    @Bean
    public WebFluxSseServerTransportProvider webFluxTransport(ObjectProvider<ObjectMapper> objectMapperProvider,
                                                              McpServerProperties serverProperties) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        return new WebFluxSseServerTransportProvider(objectMapper, "/mcp");
    }


    @Bean
    public RouterFunction<?> webfluxMcpRouterFunction(WebFluxSseServerTransportProvider webFluxProvider) {
        return webFluxProvider.getRouterFunction();
    }

  //  @Bean
    public McpSyncServer mcpServer(McpServerTransportProvider transportProvider, ToolCallback toolCallback) { // @formatter:off

        // Configure server capabilities with resource support
        var capabilities = McpSchema.ServerCapabilities.builder()
                .tools(true) // Tool support with list changes notifications
                .logging() // Logging support
                .build();

        // Create the server with both tool and resource capabilities
        McpSyncServer server = McpServer.sync(transportProvider)
                .serverInfo("MCP Demo Weather Server", "1.0.0")
                .capabilities(capabilities)
                .tools(McpToolUtils.toSyncToolSpecifications(toolCallback)) // Add @Tools
                .build();

        return server; // @formatter:on
    }




}
