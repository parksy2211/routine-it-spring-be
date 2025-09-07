package com.goormi.routine.domain.userActivity.repository;

import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.group.entity.GroupMember;
import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.userActivity.entity.ActivityType;
import com.goormi.routine.domain.userActivity.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;   // <<< 추가
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    List<UserActivity> findAllByUserAndActivityDate(User user, LocalDate activityDate);
    Optional<UserActivity>  findByUserAndActivityDateAndActivityType(User user, LocalDate activityDate, ActivityType activityType);
    List<UserActivity> findByGroupMemberInAndActivityTypeAndActivityDate(
            List<GroupMember> groupMembers, ActivityType activityType,
            LocalDate activityDate);

    List<UserActivity> findByUserIdAndActivityTypeOrderByCreatedAtDesc(Long userId, ActivityType activityType);

    long countByUserIdAndActivityTypeAndCreatedAtBetween(Long userId, ActivityType activityType, LocalDateTime startDate, LocalDateTime endDate);
    List<UserActivity> findByUserIdAndActivityTypeAndActivityDateBetween(Long userId, ActivityType activityType, LocalDate startDate, LocalDate endDate);

    // 루틴별(+날짜별) 완료 레코드 존재 여부/조회
    Optional<UserActivity>
    findByUserIdAndPersonalRoutine_RoutineIdAndActivityTypeAndActivityDate(
            Long userId, Integer routineId, ActivityType activityType, LocalDate activityDate);

    // 루틴 완료 취소 시 한방 삭제 (반환값: 삭제된 행 수)
    @Modifying
    int deleteByUserIdAndPersonalRoutine_RoutineIdAndActivityTypeAndActivityDate(
            Long userId, Integer routineId, ActivityType activityType, LocalDate activityDate);

}
