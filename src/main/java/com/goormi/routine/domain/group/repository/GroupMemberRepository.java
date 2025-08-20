package com.goormi.routine.domain.group.repository;

import com.goormi.routine.domain.group.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByMemberId(Long memberId);
    Optional<GroupMember> findByStatus(GroupMemberStatus status);
    List<GroupMember> findAllByGroupAndRole(Group group, GroupMemberRole role);
    List<GroupMember> findAllByGroupAndStatus(Group group, GroupMemberStatus status);

    Optional<GroupMember> findByGroupAndUser(Group group, User user);
}
