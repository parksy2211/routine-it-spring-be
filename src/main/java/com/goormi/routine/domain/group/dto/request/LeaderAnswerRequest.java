package com.goormi.routine.domain.group.dto.request;

import com.goormi.routine.domain.group.entity.GroupMemberRole;
import com.goormi.routine.domain.group.entity.GroupMemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class LeaderAnswerRequest {
    private Long groupId;
    private Long leaderId;
    private Long targetMemberId;
    private GroupMemberStatus status;
    private GroupMemberRole role;
    @Schema(description = "인증 요청 수락 여부")
    private boolean isApproved;
}
