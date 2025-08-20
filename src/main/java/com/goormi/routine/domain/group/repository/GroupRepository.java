package com.goormi.routine.domain.group.repository;

import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.group.entity.GroupMember;
import com.goormi.routine.domain.group.entity.GroupMemberRole;
import com.goormi.routine.domain.group.entity.GroupType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByGroupId(Long groupId);
    List<Group> findAllByUserId(Long userId);
    List<Group> findAllByGroupType(GroupType groupType);
    List<Group> findAllByIsActive(boolean isActive);
}
