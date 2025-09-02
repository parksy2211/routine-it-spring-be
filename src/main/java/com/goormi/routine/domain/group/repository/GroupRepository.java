package com.goormi.routine.domain.group.repository;

import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.group.entity.GroupType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findAllByLeaderId(Long leaderId);
    List<Group> findAllByGroupType(GroupType groupType);
    List<Group> findAllByIsActive(boolean isActive);
}
