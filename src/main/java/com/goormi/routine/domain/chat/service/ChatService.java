package com.goormi.routine.domain.chat.service;

import com.goormi.routine.domain.chat.dto.ChatMessageDto;

public interface ChatService {
    
    ChatMessageDto saveAndSendMessage(ChatMessageDto message, Long userId);
    
    // 실제 그룹 가입/탈퇴 알림 (DB 저장)
    ChatMessageDto notifyMemberJoin(Long roomId, Long userId);
    
    ChatMessageDto notifyMemberLeave(Long roomId, Long userId);
    
    // 온라인/오프라인 상태 관리 (DB 저장 안함)
    void handleUserOnline(Long roomId, Long userId);
    
    void handleUserOffline(Long roomId, Long userId);
    
    // 하위 호환성을 위해 유지 (deprecated)
    @Deprecated
    ChatMessageDto handleUserEnter(Long roomId, Long userId);
    
    @Deprecated
    ChatMessageDto handleUserLeave(Long roomId, Long userId);
}