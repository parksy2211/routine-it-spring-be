package com.goormi.routine.domain.group.service;

import com.goormi.routine.domain.group.dto.request.GroupJoinRequest;
import com.goormi.routine.domain.group.dto.request.LeaderAnswerRequest;
import com.goormi.routine.domain.group.dto.response.GroupMemberResponse;
import com.goormi.routine.domain.group.entity.GroupMemberRole;
import com.goormi.routine.domain.group.entity.GroupMemberStatus;
import com.goormi.routine.domain.group.entity.User;

import java.util.List;

public interface GroupMemberService {
    // -- Create
    GroupMemberResponse addMember(User user, GroupJoinRequest request);

    // -- Read
    List<GroupMemberResponse> getGroupsByRole(Long groupId, GroupMemberRole role);
    List<GroupMemberResponse> getGroupsByStatus(Long groupId, GroupMemberStatus status);

    // -- Update
    GroupMemberResponse updateMemberStatus(User user, LeaderAnswerRequest request);
    GroupMemberResponse updateMemberRole(User user, LeaderAnswerRequest request);

    // -- Delete
    void delete(User user, Long groupMemberId);
}
