package com.goormi.routine.domain.chat.service;

import com.goormi.routine.domain.chat.dto.ChatMessageDto;

public interface ChatService {
    
    ChatMessageDto saveAndSendMessage(ChatMessageDto message, Long userId);
    
    ChatMessageDto handleUserEnter(Long roomId, Long userId);
    
    ChatMessageDto handleUserLeave(Long roomId, Long userId);
}