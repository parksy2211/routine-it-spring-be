package com.goormi.routine.domain.group.service;

import com.goormi.routine.domain.group.dto.request.GroupJoinRequest;
import com.goormi.routine.domain.group.dto.request.LeaderAnswerRequest;
import com.goormi.routine.domain.group.dto.response.GroupMemberResponse;
import com.goormi.routine.domain.group.entity.*;
import com.goormi.routine.domain.group.repository.GroupMemberRepository;
import com.goormi.routine.domain.group.repository.GroupRepository;
import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupMemberServiceImpl implements GroupMemberService {
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    // 그룹에 멤버가 참여 신청시 펜딩으로 추가
    @Override
    public GroupMemberResponse addMember(Long userId, Long groupId, GroupJoinRequest request) {

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(()->new IllegalArgumentException("Group not found"));

        if(!Objects.equals(groupId, request.getGroupId())){
            throw new IllegalArgumentException("Invalid group id");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("User not found"));

        Optional<GroupMember> existingMember = groupMemberRepository
                .findByGroupAndUser(group, user);

        if (existingMember.isPresent()) {
            GroupMember member = existingMember.get();
            if (member.getStatus() == GroupMemberStatus.BLOCKED) {
                throw new IllegalArgumentException("BLOCKED Member");
            }
            if (member.getStatus() == GroupMemberStatus.JOINED ||
                    member.getStatus() == GroupMemberStatus.PENDING) {
                throw new IllegalArgumentException("이미 참여 중이거나 대기 중입니다.");
            }
        }

        GroupMember groupMember = group.addMember(user); // PENDING

        return GroupMemberResponse.from(groupMember);
    }

    // -- Read
    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupsByRole(Long groupId, GroupMemberRole role) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(()->new IllegalArgumentException("Group not found"));

        List<GroupMember> groupMembers = groupMemberRepository.findAllByGroupAndRole(group, role);
        return groupMembers.stream()
                .map(GroupMemberResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupsByStatus(Long groupId, GroupMemberStatus status) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(()->new IllegalArgumentException("Group not found"));

        List<GroupMember> groupMembers = groupMemberRepository.findAllByGroupAndStatus(group, status);
        return groupMembers.stream()
                .map(GroupMemberResponse::from)
                .toList();
    }


    // -- Update
    @Override
    public GroupMemberResponse updateMemberStatus(Long leaderId, LeaderAnswerRequest request) {
        Group group = validateLeader(leaderId, request);
        GroupMember groupMember = validateMember(request);

        GroupMemberStatus oldStatus = groupMember.getStatus();
        GroupMemberStatus newStatus = request.getStatus();

        if (oldStatus == GroupMemberStatus.JOINED) {
            if (newStatus != GroupMemberStatus.JOINED) {
                group.minusMemberCnt();
            }
        }
        else{
            if (newStatus == GroupMemberStatus.JOINED) {
                group.addMemberCnt();
            }
        }
        groupMember.changeStatus(newStatus); // JOINED, BLOCKED, LEFT

        return GroupMemberResponse.from(groupMember);
    }

    @Override
    public GroupMemberResponse updateMemberRole(Long leaderId, LeaderAnswerRequest request) {
        validateLeader(leaderId, request);

        GroupMember groupMember = validateMember(request);

        groupMember.changeRole(request.getRole()); // LEADER, MEMBER

        return GroupMemberResponse.from(groupMember);
    }

    private Group validateLeader(Long leaderId, LeaderAnswerRequest request) {
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(()->new IllegalArgumentException("Group not found"));

        if(!Objects.equals(leaderId, group.getLeader().getId())){
            throw new IllegalArgumentException(" is not the leader of this member");
        }
        return group;
    }

    private GroupMember validateMember(LeaderAnswerRequest request) {
        GroupMember groupMember = groupMemberRepository.findById(request.getTargetMemberId())
                .orElseThrow(()->new IllegalArgumentException("Member not found"));

        if (!Objects.equals(groupMember.getGroup().getGroupId(), request.getGroupId())) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아님");
        }
        return groupMember;
    }

    // -- Delete
    @Override
    public void delete(Long userId, Long groupId) { // 본인이 탈퇴하는 것, 리더는 블락 사용
        Group group = groupRepository.findById(groupId).
                orElseThrow(()->new IllegalArgumentException("Group not found"));

        User user = userRepository.findById(userId).
                orElseThrow(()->new IllegalArgumentException("User not found"));

        GroupMember groupMember = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(()-> new IllegalArgumentException("Group Member not found"));

        if (groupMember.getRole() == GroupMemberRole.LEADER) {
            throw new IllegalArgumentException("리더는 떠날 수 없습니다. 리더를 위임해주세요");
        }
        groupMember.changeStatus(GroupMemberStatus.LEFT);
//        groupMemberRepository.delete(groupMember);
    }
}
