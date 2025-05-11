package com.lkl.ai.mcp.webflux;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hi")
    public String getHello() {
        return "Hello World!" ;
    }

}