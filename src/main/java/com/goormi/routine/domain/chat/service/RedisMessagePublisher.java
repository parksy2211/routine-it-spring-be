package com.goormi.routine.domain.chat.service;

import com.goormi.routine.domain.chat.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessagePublisher {
    
    private final RedisTemplate<String, Object> chatRedisTemplate;
    private final ChannelTopic chatTopic;
    
    public void publish(ChatMessageDto message) {
        log.info("Publishing message to Redis: roomId={}, message={}", message.getRoomId(), message.getMessage());
        chatRedisTemplate.convertAndSend(chatTopic.getTopic(), message);
    }
}