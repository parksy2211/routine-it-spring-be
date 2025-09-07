package com.goormi.routine.domain.chat.controller;

import com.goormi.routine.domain.chat.dto.ChatMessageDto;
import com.goormi.routine.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    
    @MessageMapping("/chat.send/{roomId}")
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Payload ChatMessageDto message,
            Principal principal) {
        
        log.info("Received message in room {}: {}", roomId, message.getMessage());
        
        message.setRoomId(roomId);
        Long userId = Long.parseLong(principal.getName());
        
        chatService.saveAndSendMessage(message, userId);
    }
    
    @MessageMapping("/chat.enter/{roomId}")
    public void enterRoom(
            @DestinationVariable Long roomId,
            @Payload ChatMessageDto message,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        
        log.info("User {} entered room {}", principal.getName(), roomId);
        
        headerAccessor.getSessionAttributes().put("roomId", roomId);
        headerAccessor.getSessionAttributes().put("userId", principal.getName());
        
        Long userId = Long.parseLong(principal.getName());
        chatService.handleUserEnter(roomId, userId);
    }
    
    @MessageMapping("/chat.leave/{roomId}")
    public void leaveRoom(
            @DestinationVariable Long roomId,
            @Payload ChatMessageDto message,
            Principal principal) {
        
        log.info("User {} left room {}", principal.getName(), roomId);
        
        Long userId = Long.parseLong(principal.getName());
        chatService.handleUserLeave(roomId, userId);
    }
    
    @MessageMapping("/chat.online/{roomId}")
    public void userOnline(
            @DestinationVariable Long roomId,
            Principal principal) {
        
        log.debug("User {} is now online in room {}", principal.getName(), roomId);
        
        Long userId = Long.parseLong(principal.getName());
        chatService.handleUserOnline(roomId, userId);
    }
    
    @MessageMapping("/chat.offline/{roomId}")
    public void userOffline(
            @DestinationVariable Long roomId,
            Principal principal) {
        
        log.debug("User {} is now offline in room {}", principal.getName(), roomId);
        
        Long userId = Long.parseLong(principal.getName());
        chatService.handleUserOffline(roomId, userId);
    }
}