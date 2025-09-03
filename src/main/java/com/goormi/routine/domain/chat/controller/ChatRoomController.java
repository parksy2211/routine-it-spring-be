package com.goormi.routine.domain.chat.controller;

import com.goormi.routine.common.response.ApiResponse;
import com.goormi.routine.domain.chat.dto.ChatMessageDto;
import com.goormi.routine.domain.chat.dto.ChatRoomDto;
import com.goormi.routine.domain.chat.dto.CreateChatRoomRequest;
import com.goormi.routine.domain.chat.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.goormi.routine.domain.user.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chat Room", description = "채팅방 관리 API")
@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {
    
    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;
    
    // 그룹 생성 시 자동으로 채팅방이 생성되므로 별도 채팅방 생성 API는 비활성화
    // @Operation(summary = "채팅방 생성", description = "새로운 채팅방을 생성합니다")
    // @PostMapping
    // public ResponseEntity<ApiResponse<ChatRoomDto>> createRoom(
    //         @Valid @RequestBody CreateChatRoomRequest request,
    //         @AuthenticationPrincipal UserDetails userDetails) {
    //     
    //     ChatRoomDto room = chatRoomService.createRoom(request, userDetails.getUsername());
    //     return ResponseEntity.ok(ApiResponse.success(room));
    // }
    
    @Operation(summary = "그룹의 채팅방 조회", description = "특정 그룹의 채팅방을 조회합니다")
    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<ChatRoomDto>> getRoomByGroupId(@PathVariable Long groupId) {
        ChatRoomDto room = chatRoomService.getRoomByGroupId(groupId);
        return ResponseEntity.ok(ApiResponse.success(room));
    }
    
    @Operation(summary = "채팅방 목록 조회", description = "참여 가능한 채팅방 목록을 조회합니다")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ChatRoomDto>>> getRooms(
            @RequestParam(required = false) Long groupId,
            Pageable pageable) {
        
        Page<ChatRoomDto> rooms = chatRoomService.getRooms(groupId, pageable);
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }
    
    @Operation(summary = "내가 참여한 채팅방 목록", description = "사용자가 참여한 채팅방 목록을 조회합니다")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ChatRoomDto>>> getMyRooms(
            @AuthenticationPrincipal Long userId) {
        
        String username = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"))
                .getEmail();
        
        List<ChatRoomDto> rooms = chatRoomService.getMyRooms(username);
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }
    
    @Operation(summary = "채팅방 상세 조회", description = "특정 채팅방의 상세 정보를 조회합니다")
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomDto>> getRoom(@PathVariable Long roomId) {
        
        ChatRoomDto room = chatRoomService.getRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success(room));
    }
    
    @Operation(summary = "채팅방 참여", description = "채팅방에 참여합니다")
    @PostMapping("/{roomId}/join")
    public ResponseEntity<ApiResponse<Void>> joinRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal Long userId) {
        
        String username = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"))
                .getEmail();
        
        chatRoomService.joinRoom(roomId, username);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @Operation(summary = "채팅방 나가기", description = "채팅방에서 나갑니다")
    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal Long userId) {
        
        String username = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"))
                .getEmail();
        
        chatRoomService.leaveRoom(roomId, username);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @Operation(summary = "이전 메시지 조회", description = "채팅방의 이전 메시지를 조회합니다")
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<Page<ChatMessageDto>>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(required = false) Long beforeMessageId,
            Pageable pageable,
            @AuthenticationPrincipal Long userId) {
        
        String username = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"))
                .getEmail();
        
        Page<ChatMessageDto> messages = chatRoomService.getMessages(roomId, beforeMessageId, pageable, username);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }
}