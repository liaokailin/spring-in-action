package com.lkl.mcp.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class MvcSseController {

    @GetMapping("/hi")
    public String getHello() {
        return "Hello World!" ;
    }

}