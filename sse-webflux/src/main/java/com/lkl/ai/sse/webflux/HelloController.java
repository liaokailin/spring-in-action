package com.lkl.ai.sse.webflux;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
@RestController
public class HelloController {

    @GetMapping("/sse-flux")
    public Flux<ServerSentEvent<String>> handleSseFlux() {
        return Flux.interval(Duration.ofMillis(100))
                .map(sequence -> ServerSentEvent.<String>builder()
                        .id(String.valueOf(sequence))
                        .event("事件")
                        .data("SSE in WebFlux - " + sequence)
                        .build());
    }
}
