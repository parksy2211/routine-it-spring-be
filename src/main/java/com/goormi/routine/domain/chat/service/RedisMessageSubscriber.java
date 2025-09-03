package com.goormi.routine.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormi.routine.domain.chat.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessageSubscriber {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    
    public void onMessage(String message) {
        try {
            ChatMessageDto chatMessage = objectMapper.readValue(message, ChatMessageDto.class);
            log.info("Received message from Redis: roomId={}, message={}", chatMessage.getRoomId(), chatMessage.getMessage());
            
            messagingTemplate.convertAndSend("/topic/room/" + chatMessage.getRoomId(), chatMessage);
        } catch (Exception e) {
            log.error("Error processing Redis message", e);
        }
    }
}