package com.lkl.test.spring.docker;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
public class RedisController {

    @Resource
    private RedisTemplate<String, String> redisTemplate;


    @GetMapping("/add/test")
    public String add() {
        String key = "test";
        String value = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(key, value);
        return "添加成功";
    }

    @GetMapping("/query/test")
    public String query() {
        String key = "test";
        return "查询成功:" + redisTemplate.opsForValue().get(key);
    }


}
