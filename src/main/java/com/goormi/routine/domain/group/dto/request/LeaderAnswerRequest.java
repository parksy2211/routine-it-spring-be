package com.goormi.routine.domain.group.dto.request;

import com.goormi.routine.domain.group.entity.GroupMemberRole;
import com.goormi.routine.domain.group.entity.GroupMemberStatus;
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
    private boolean authApproved;
}
