package com.goormi.routine.domain.chat.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDto {
    
    private Long id;
    private Long groupId;
    private String roomName;
    private String description;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private Boolean isActive;
    private Long createdBy;
    private String creatorNickname;
    private LocalDateTime createdAt;
}