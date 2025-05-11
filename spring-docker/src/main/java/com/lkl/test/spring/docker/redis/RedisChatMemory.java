package com.lkl.test.spring.docker.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RedisChatMemory implements ChatMemory {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final String memoryKeyPrefix = "chat:memory:prefix:"; // Redis Key 前缀

    public RedisChatMemory(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取指定会话的最新 N 条消息（按时间倒序）
     */
    @Override
    public List<Message> get(String conversationId, int lastN) {
        String key = memoryKeyPrefix + conversationId;
        long totalMessages = redisTemplate.opsForList().size(key);

        if (totalMessages == 0) {
            return Collections.emptyList();
        }

        // 计算起始和结束索引（获取最后 N 条）
        long start = Math.max(0, totalMessages - lastN);
        long end = totalMessages - 1;

        // 获取指定范围的消息
        List<String> messageJsons = redisTemplate.opsForList().range(key, start, end);

        return messageJsons.stream().map(this::deserializeMessage).collect(Collectors.toList());
    }

    /**
     * 添加消息到会话历史
     */
    @Override
    public void add(String conversationId, Message message) {
        String key = memoryKeyPrefix + conversationId;
        String messageJson = serializeMessage(message);

        // 使用 LPUSH 或 RPUSH 存储消息（LPUSH 表示最新消息在列表头部）
        redisTemplate.opsForList().rightPush(key, messageJson);


        // 可选：设置 Key 的过期时间（例如 30 天）
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
    }

    /**
     * 批量添加消息到会话历史（高性能实现）
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        String key = memoryKeyPrefix + conversationId;
        ListOperations<String, String> listOps = redisTemplate.opsForList();

        // 序列化并批量添加消息
        messages.forEach(message -> {
            String messageJson = serializeMessage(message);
            listOps.rightPush(key, messageJson);
        });

        // 设置 Key 的过期时间（30天）
        redisTemplate.expire(key, 30, TimeUnit.DAYS);

        // 可以考虑使用 Redis Pipeline 批量操作（减少网络开销）
    }

    /**
     * 清除指定会话的记忆
     */
    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(memoryKeyPrefix + conversationId);
    }


    private String serializeMessage(Message message) {
        Map<String, Object> map = new HashMap<>();
        AbstractMessage abstractMessage = null;
        if (message instanceof AbstractMessage) {
            abstractMessage = (AbstractMessage) message;
        }

        map.put("type", abstractMessage.getMessageType().getValue());
        map.put("content", abstractMessage.getText());
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Message deserializeMessage(String json) {
        try {
            Map<String, String> map = objectMapper.readValue(json, Map.class);
            String type = map.get("type");
            String content = map.get("content");
            return switch (type) {
                case "user" -> new UserMessage(content);
                case "assistant" -> new AssistantMessage(content);
                default -> throw new IllegalArgumentException("Unknown type: " + type);
            };
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}