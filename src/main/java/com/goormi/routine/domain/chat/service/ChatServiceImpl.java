package com.goormi.routine.domain.chat.service;

import com.goormi.routine.domain.chat.dto.ChatMessageDto;
import com.goormi.routine.domain.chat.entity.ChatMember;
import com.goormi.routine.domain.chat.entity.ChatMessage;
import com.goormi.routine.domain.chat.entity.ChatMessage.MessageType;
import com.goormi.routine.domain.chat.repository.ChatMemberRepository;
import com.goormi.routine.domain.chat.repository.ChatMessageRepository;
import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {
    
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final UserRepository userRepository;
    private final RedisMessagePublisher redisMessagePublisher;
    
    @Override
    public ChatMessageDto saveAndSendMessage(ChatMessageDto messageDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        
        ChatMember member = chatMemberRepository.findByRoomIdAndUserIdAndIsActiveTrue(messageDto.getRoomId(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방에 참여하지 않은 사용자입니다"));
        
        ChatMessage message = ChatMessage.builder()
                .roomId(messageDto.getRoomId())
                .userId(user.getId())
                .senderNickname(user.getNickname())
                .message(messageDto.getMessage())
                .messageType(MessageType.TALK)
                .build();
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        chatMemberRepository.updateLastReadMessage(messageDto.getRoomId(), user.getId(), savedMessage.getId());
        
        ChatMessageDto dto = convertToDto(savedMessage);
        redisMessagePublisher.publish(dto);
        
        return dto;
    }
    
    @Override
    public ChatMessageDto handleUserEnter(Long roomId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        
        ChatMessage message = ChatMessage.builder()
                .roomId(roomId)
                .userId(user.getId())
                .senderNickname(user.getNickname())
                .message(user.getNickname() + "님이 입장하셨습니다.")
                .messageType(MessageType.ENTER)
                .build();
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        ChatMessageDto dto = convertToDto(savedMessage);
        redisMessagePublisher.publish(dto);
        
        return dto;
    }
    
    @Override
    public ChatMessageDto handleUserLeave(Long roomId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        
        ChatMessage message = ChatMessage.builder()
                .roomId(roomId)
                .userId(user.getId())
                .senderNickname(user.getNickname())
                .message(user.getNickname() + "님이 퇴장하셨습니다.")
                .messageType(MessageType.LEAVE)
                .build();
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        ChatMessageDto dto = convertToDto(savedMessage);
        redisMessagePublisher.publish(dto);
        
        return dto;
    }
    
    private ChatMessageDto convertToDto(ChatMessage message) {
        return ChatMessageDto.builder()
                .id(message.getId())
                .roomId(message.getRoomId())
                .userId(message.getUserId())
                .senderNickname(message.getSenderNickname())
                .message(message.getMessage())
                .messageType(message.getMessageType())
                .sentAt(message.getSentAt())
                .build();
    }
}