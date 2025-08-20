package com.goormi.routine.domain.group.service;

import com.goormi.routine.domain.group.dto.request.GroupCreateRequest;
import com.goormi.routine.domain.group.dto.request.GroupUpdateRequest;
import com.goormi.routine.domain.group.dto.response.GroupResponse;
import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.group.entity.GroupType;
import com.goormi.routine.domain.group.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface GroupService {
    // -- create
    GroupResponse createGroup(User leader, GroupCreateRequest request);

    // -- Read
    GroupResponse getGroupInfo(Long groupId);
    List<GroupResponse> getGroupsByUserId(Long userId);
    List<GroupResponse> getGroupsByGroupType(GroupType groupType);
    List<GroupResponse> getGroupsByIsActive(boolean isActive);

    // -- Update
    GroupResponse updateGroupInfo(User user, Long groupId, GroupUpdateRequest request);

    // -- Delete
    void deleteGroup(User user, Long groupId);
}
