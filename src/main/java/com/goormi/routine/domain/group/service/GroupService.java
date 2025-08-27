package com.goormi.routine.domain.group.service;

import com.goormi.routine.domain.group.dto.request.GroupCreateRequest;
import com.goormi.routine.domain.group.dto.request.GroupUpdateRequest;
import com.goormi.routine.domain.group.dto.response.GroupResponse;
import com.goormi.routine.domain.group.entity.GroupType;

import java.util.List;

public interface GroupService {
    // -- create
    GroupResponse createGroup(Long leaderId, GroupCreateRequest request);

    // -- Read
    GroupResponse getGroupInfo(Long groupId);
    List<GroupResponse> getGroupsByUserId(Long userId);
    List<GroupResponse> getGroupsByGroupType(GroupType groupType);
    List<GroupResponse> getGroupsByIsActive(boolean isActive);

    // -- Update
    GroupResponse updateGroupInfo(Long leaderId, Long groupId, GroupUpdateRequest request);

    // -- Delete
    void deleteGroup(Long userId, Long groupId);
}
