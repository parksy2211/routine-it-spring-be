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

    int countByGroupId(Long groupId);

    @Query("""
        SELECT COUNT(DISTINCT gm.user.id)
        FROM GroupMember gm
        JOIN Auth a ON gm.user.id = a.user.id AND gm.group.id = a.group.id
        WHERE gm.group.id = :groupId
          AND FUNCTION('DATE_FORMAT', a.createdAt, '%Y-%m') = :monthYear
    """)
    int countActiveByGroupId(@Param("groupId") Long groupId,
        @Param("monthYear") String monthYear);

    @Query("""
        SELECT COUNT(a)
        FROM Auth a
        WHERE a.group.id = :groupId
          AND FUNCTION('DATE_FORMAT', a.createdAt, '%Y-%m') = :monthYear
    """)
    int countAuthByGroupId(@Param("groupId") Long groupId,
        @Param("monthYear") String monthYear);
}
