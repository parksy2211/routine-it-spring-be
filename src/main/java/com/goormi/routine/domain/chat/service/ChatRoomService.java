package com.goormi.routine.domain.chat.service;

import com.goormi.routine.domain.chat.dto.ChatMessageDto;
import com.goormi.routine.domain.chat.dto.ChatRoomDto;
import com.goormi.routine.domain.chat.dto.CreateChatRoomRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatRoomService {
    
    ChatRoomDto createRoom(CreateChatRoomRequest request, String username);
    
    Page<ChatRoomDto> getRooms(Long groupId, Pageable pageable);
    
    List<ChatRoomDto> getMyRooms(String username);
    
    ChatRoomDto getRoom(Long roomId);
    
    ChatRoomDto getRoomByGroupId(Long groupId);
    
    void joinRoom(Long roomId, String username);
    
    void leaveRoom(Long roomId, String username);
    
    Page<ChatMessageDto> getMessages(Long roomId, Long beforeMessageId, Pageable pageable, String username);
}