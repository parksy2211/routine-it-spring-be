package com.goormi.routine.domain.group.repository;

import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.group.entity.GroupMember;
import com.goormi.routine.domain.group.entity.GroupMemberRole;
import com.goormi.routine.domain.group.entity.GroupMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByMemberId(Long memberId);
    Optional<GroupMember> findByGroupAndRole(Group group, GroupMemberRole role);
    List<GroupMember> findAllByGroupAndRole(Group group, GroupMemberRole role);
    List<GroupMember> findAllByGroupAndStatus(Group group, GroupMemberStatus status);

}
