package com.goormi.routine.domain.group.service;

import com.goormi.routine.domain.group.dto.request.GroupJoinRequest;
import com.goormi.routine.domain.group.dto.request.LeaderAnswerRequest;
import com.goormi.routine.domain.group.dto.response.GroupMemberResponse;
import com.goormi.routine.domain.group.entity.GroupMemberRole;
import com.goormi.routine.domain.group.entity.GroupMemberStatus;

import java.util.List;

public interface GroupMemberService {
    // -- Create
    GroupMemberResponse addMember(Long userId, Long groupId, GroupJoinRequest request);

    // -- Read
    List<GroupMemberResponse> getGroupMembersByRole(Long groupId, GroupMemberRole role);
    List<GroupMemberResponse> getGroupMembersByStatus(Long groupId, GroupMemberStatus status);

    // -- Update
    GroupMemberResponse updateMemberStatus(Long leaderId, LeaderAnswerRequest request);
    GroupMemberResponse updateMemberRole(Long leaderId, LeaderAnswerRequest request);

    // -- Delete
    void delete(Long userId, Long groupId);
}
