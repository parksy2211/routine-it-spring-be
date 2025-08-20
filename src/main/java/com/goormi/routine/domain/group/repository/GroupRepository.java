package com.goormi.routine.domain.group.repository;

import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.group.entity.GroupMember;
import com.goormi.routine.domain.group.entity.GroupMemberRole;
import com.goormi.routine.domain.group.entity.GroupType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByGroupId(Long groupId);
    List<Group> findAllByMemberId(Long memberId);
    List<Group> findAllByGroupType(GroupType groupType);
    List<Group> findAllByIsActive(boolean isActive);
}
