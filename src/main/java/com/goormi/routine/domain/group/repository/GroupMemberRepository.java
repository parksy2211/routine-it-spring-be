package com.goormi.routine.domain.group.repository;

import com.goormi.routine.domain.group.entity.*;
import com.goormi.routine.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByGroupAndUser(Group group, User user);
    List<GroupMember> findAllByGroupAndRole(Group group, GroupMemberRole role);
    List<GroupMember> findAllByGroupAndStatus(Group group, GroupMemberStatus status);
	List<GroupMember> findAllByUserAndStatus(User user, GroupMemberStatus status);


	@Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.groupId = :groupId")
	int countMembersByGroupId(@Param("groupId") Long groupId);

	// 인증 엔티티가 구현되면 활성화
	// @Query("""
	//     SELECT COUNT(DISTINCT gm.user.id)
	//     FROM GroupMember gm
	//     JOIN Auth a ON gm.user.id = a.user.id AND gm.group.groupId = a.group.groupId
	//     WHERE gm.group.groupId = :groupId
	//       AND FUNCTION('DATE_FORMAT', a.createdAt, '%Y-%m') = :monthYear
	// """)
	// int countActiveByGroupId(@Param("groupId") Long groupId,
	//     @Param("monthYear") String monthYear);

	// @Query("""
	//     SELECT COUNT(a)
	//     FROM Auth a
	//     WHERE a.group.groupId = :groupId
	//       AND FUNCTION('DATE_FORMAT', a.createdAt, '%Y-%m') = :monthYear
	// """)
	// int countAuthByGroupId(@Param("groupId") Long groupId,
	//     @Param("monthYear") String monthYear);

	@Query("SELECT gm FROM GroupMember gm WHERE gm.user.id = :userId AND gm.status = 'JOINED'")
	List<GroupMember> findActiveGroupsByUserId(@Param("userId") Long userId);
}
