package com.goormi.routine.domain.group.service;

import com.goormi.routine.domain.group.dto.request.GroupCreateRequest;
import com.goormi.routine.domain.group.dto.request.GroupUpdateRequest;
import com.goormi.routine.domain.group.dto.response.GroupResponse;
import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.group.entity.GroupType;
import com.goormi.routine.domain.group.entity.User;
import com.goormi.routine.domain.group.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;

    // -- create
    @Override
    public GroupResponse createGroup(User leader, GroupCreateRequest request) {
        // User leader = userRepository.findById(userId); 밑의 메소드들도 userFind 후 작업하도록 수정해야함.

        Group group = Group.createGroup(leader,
                request.getGroupName(),
                request.getGroupDescription(),
                request.getGroupType());

        group.addLeader(leader);
        groupRepository.save(group);
        return GroupResponse.from(group);
    }

    // -- Read
    @Override
    @Transactional(readOnly = true)
    public GroupResponse getGroupInfo(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(groupId + " is not found"));

        return GroupResponse.from(group);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getGroupsByUserId(Long userId){
        List<Group> groups = groupRepository.findAllByLeaderId(userId);
        return groups.stream()
                .map(GroupResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getGroupsByGroupType(GroupType groupType){
        List<Group> groups = groupRepository.findAllByGroupType(groupType);
        return  groups.stream()
                .map(GroupResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getGroupsByIsActive(boolean isActive){
        List<Group> groups = groupRepository.findAllByIsActive(isActive);
        return groups.stream()
                .map(GroupResponse::from)
                .toList();
    }



    // -- Update
    @Override
    @Transactional
    public GroupResponse updateGroupInfo(User user, Long groupId, GroupUpdateRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(groupId + " is not found"));

        if (!Objects.equals(user.getId(), group.getLeader().getId())){
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        group.updateBasicInfo(request.getGroupName(), request.getGroupDescription(), request.getGroupType());
        group.updateTimeInfo(request.getAlarmTime(), request.getAuthDays());
        group.updateOtherInfo(request.getCategory(), request.getImageUrl(), request.getMaxMembers());

        groupRepository.save(group);
        return GroupResponse.from(group);
    }

    // -- Delete
    @Override
    @Transactional
    public void deleteGroup(User user, Long groupId){
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(groupId + " is not found"));

        if (!Objects.equals(user.getId(), group.getLeader().getId())){
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        group.deactivate(); // 비활성화 후 일정기간 후 삭제?
        groupRepository.delete(group);
    }

}
