package com.goormi.routine.domain.group.service;

import com.goormi.routine.domain.group.dto.request.GroupJoinRequest;
import com.goormi.routine.domain.group.dto.request.LeaderAnswerRequest;
import com.goormi.routine.domain.group.dto.response.GroupMemberResponse;
import com.goormi.routine.domain.group.entity.*;
import com.goormi.routine.domain.group.repository.GroupMemberRepository;
import com.goormi.routine.domain.group.repository.GroupRepository;
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

    // -- Create
    // 그룹에 멤버 가입
    @Override
    public GroupMemberResponse addMember(User user, GroupJoinRequest request) {
        Group group = groupRepository.findByGroupId(request.getGroupId())
                .orElseThrow(()->new IllegalArgumentException("Group not found"));

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
        groupRepository.save(group);
        groupMemberRepository.save(groupMember);

        return GroupMemberResponse.from(groupMember);
    }

    // -- Read
    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupsByRole(Long groupId, GroupMemberRole role) {
        Group group = groupRepository.findByGroupId(groupId)
                .orElseThrow(()->new IllegalArgumentException("Group not found"));

        List<GroupMember> groupMembers = groupMemberRepository.findAllByGroupAndRole(group, role);
        return groupMembers.stream()
                .map(GroupMemberResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupsByStatus(Long groupId, GroupMemberStatus status) {
        Group group = groupRepository.findByGroupId(groupId)
                .orElseThrow(()->new IllegalArgumentException("Group not found"));

        List<GroupMember> groupMembers = groupMemberRepository.findAllByGroupAndStatus(group, status);
        return groupMembers.stream()
                .map(GroupMemberResponse::from)
                .toList();
    }


    // -- Update
    @Override
    public GroupMemberResponse updateMemberStatus(User leader, LeaderAnswerRequest request) {
        if(!Objects.equals(leader.getId(), request.getLeaderId())){
            throw new IllegalArgumentException(" is not the leader of this member");
        }
        Group group = groupRepository.findByGroupId(request.getGroupId())
                .orElseThrow(()->new IllegalArgumentException("Group not found"));

        GroupMember groupMember = groupMemberRepository.findByMemberId(request.getTargetMemberId())
                .orElseThrow(()->new IllegalArgumentException("Member not found"));

        if (!Objects.equals(groupMember.getGroup().getGroupId(), request.getGroupId())) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아님");
        }

        groupMember.changeStatus(request.getStatus()); // JOINED, BLOCKED, LEFT

        if (request.getStatus() == GroupMemberStatus.BLOCKED || request.getStatus() == GroupMemberStatus.LEFT) {
            group.removeMember(leader);
            groupRepository.save(group);
        }

        groupMemberRepository.save(groupMember);
        return GroupMemberResponse.from(groupMember);
    }

    @Override
    public GroupMemberResponse updateMemberRole(User user, LeaderAnswerRequest request) {
        if(!Objects.equals(user.getId(), request.getLeaderId())){
            throw new IllegalArgumentException(" is not the leader of this member");
        }

        GroupMember groupMember = groupMemberRepository.findByMemberId(request.getTargetMemberId())
                .orElseThrow(()->new IllegalArgumentException("Member not found"));

        if (!Objects.equals(groupMember.getGroup().getGroupId(), request.getGroupId())) {
            throw new IllegalArgumentException("해당 그룹의 멤버가 아님");
        }

        groupMember.changeRole(request.getRole()); // LEADER, MEMBER
        groupMemberRepository.save(groupMember);

        return GroupMemberResponse.from(groupMember);
    }

    // -- Delete
    @Override
    public void delete(User user, Long groupMemberId) { // 본인이 탈퇴하는 것, 리더는 블락 사용

        GroupMember groupMember = groupMemberRepository.findByMemberId(groupMemberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (!Objects.equals(user.getId(), groupMember.getUser().getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        groupMemberRepository.delete(groupMember);
    }

}
