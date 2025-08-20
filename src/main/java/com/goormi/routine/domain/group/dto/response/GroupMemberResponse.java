package com.goormi.routine.domain.group.dto.response;

import com.goormi.routine.domain.group.entity.GroupMember;
import com.goormi.routine.domain.group.entity.GroupMemberRole;
import com.goormi.routine.domain.group.entity.GroupMemberStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GroupMemberResponse {
    private String groupName;
    private GroupMemberStatus status;
    private GroupMemberRole role;
    private String message;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static  GroupMemberResponse from(GroupMember groupMember) {
        return GroupMemberResponse.builder()
                .groupName(groupMember.getGroup().getGroupName())
                .status(groupMember.getStatus())
                .role(groupMember.getRole())
                .createdAt(groupMember.getCreatedAt())
                .updatedAt(groupMember.getUpdatedAt())
                .build();
    }
}
