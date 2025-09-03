package com.goormi.routine.domain.group.repository;

import com.goormi.routine.domain.group.entity.*;
import com.goormi.routine.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByGroupAndUser(Group group, User user);
    List<GroupMember> findAllByGroupAndRole(Group group, GroupMemberRole role);
    List<GroupMember> findAllByGroupAndStatus(Group group, GroupMemberStatus status);
    List<GroupMember> findAllByUserAndStatus(User user, GroupMemberStatus status);
  
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.user.id = :userId AND gm.status = 'JOINED'")
    Optional<GroupMember> findByGroupIdAndUserIdAndIsActiveTrue(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
