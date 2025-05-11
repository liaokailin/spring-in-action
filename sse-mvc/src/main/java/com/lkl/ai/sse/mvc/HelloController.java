package com.lkl.ai.sse.mvc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class HelloController {

    @GetMapping("/sse-mvc")
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter(30000L); // 超时时间 30 秒
        // 模拟推送事件
        new Thread(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    emitter.send("Event " + i);
                    Thread.sleep(100);
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }
}
