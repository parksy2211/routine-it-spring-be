package com.goormi.routine.domain.group.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class GroupJoinRequest {
    private Long groupId;
    private Long groupMemberId;
}
