package com.goormi.routine.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateChatRoomRequest {
    
    @NotNull(message = "그룹 ID는 필수입니다")
    private Long groupId;
    
    @NotBlank(message = "채팅방 이름은 필수입니다")
    @Size(min = 2, max = 100, message = "채팅방 이름은 2-100자 사이여야 합니다")
    private String roomName;
    
    @Size(max = 500, message = "설명은 500자 이내여야 합니다")
    private String description;
    
    private Integer maxParticipants = 100;
}