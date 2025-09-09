package com.goormi.routine.domain.userActivity.service;

import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.group.entity.GroupMember;
import com.goormi.routine.domain.group.repository.GroupMemberRepository;
import com.goormi.routine.domain.group.repository.GroupRepository;
import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.user.repository.UserRepository;
import com.goormi.routine.domain.userActivity.dto.UserActivityRequest;
import com.goormi.routine.domain.userActivity.dto.UserActivityResponse;
import com.goormi.routine.domain.userActivity.entity.ActivityType;
import com.goormi.routine.domain.userActivity.entity.UserActivity;
import com.goormi.routine.domain.userActivity.repository.UserActivityRepository;
import com.goormi.routine.personal_routines.domain.PersonalRoutine;
import com.goormi.routine.personal_routines.repository.PersonalRoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class UserActivityServiceImpl implements UserActivityService{
    private final UserActivityRepository userActivityRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final PersonalRoutineRepository personalRoutineRepository;

    @Override
    public UserActivityResponse create(Long userId, UserActivityRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserActivity userActivity;

        if (request.getActivityType() == ActivityType.GROUP_AUTH_COMPLETE) {
            if (request.getGroupId() == null) throw new IllegalArgumentException("GroupId is null");

            Group group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("Group not found"));
            GroupMember groupMember = groupMemberRepository.findByGroupAndUser(group, user)
                    .orElseThrow(() -> new IllegalArgumentException("GroupMember not found"));

            userActivity = UserActivity.createActivity(user, groupMember, request.getImageUrl());

        }
        else if (request.getActivityType() == ActivityType.PERSONAL_ROUTINE_COMPLETE) {
            if (request.getPersonalRoutineId() == null) throw new IllegalArgumentException("PersonalRoutine Id is null");

            PersonalRoutine personalRoutine = personalRoutineRepository.findById(request.getPersonalRoutineId())
                    .orElseThrow(() -> new IllegalArgumentException("Personal Routine not found"));
            userActivity = UserActivity.createActivity(user, personalRoutine);
        }
        else if (request.getActivityType() == ActivityType.DAILY_CHECKLIST) {
            userActivity = UserActivity.builder()
                    .user(user)
                    .activityType(ActivityType.DAILY_CHECKLIST)
                    .activityDate(LocalDate.now())
                    .createdAt(LocalDateTime.now())
                    .isPublic(false)
                    .build();
        } else{
          throw new  IllegalArgumentException("Invalid request");
        }

        UserActivity saved = userActivityRepository.save(userActivity);
        return convertToResponse(saved);
    }

    @Override
    public UserActivityResponse updateActivity(Long userId, UserActivityRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserActivity userActivity = userActivityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        if (!Objects.equals(user.getId(), userActivity.getUser().getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        userActivity.updateActivity(request.getActivityType(), request.getIsPublic());
        return convertToResponse(userActivity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserActivityResponse> getUserActivitiesPerDay(Long userId, LocalDate activityDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<UserActivity> activities = userActivityRepository.findAllByUserAndActivityDate(user, activityDate);
        return activities.stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserActivityResponse> getImagesOfUserActivities(Long currentUserId, Long targetUserId) {
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));

        List<UserActivity> activities = userActivityRepository
                .findByUserIdAndActivityTypeOrderByCreatedAtDesc(targetUserId, ActivityType.GROUP_AUTH_COMPLETE);

        boolean isOwner = targetUserId.equals(currentUserId);

        if (!isOwner) {
            // 본인이 아니면 공개된 사진만 조회
            return activities.stream()
                    .filter(UserActivity::getIsPublic)
                    .map(this::convertToResponse)
                    .toList();
        }

        return activities.stream()
                .map(this::convertToResponse)
                .toList();
    }


    private UserActivityResponse convertToResponse(UserActivity activity) {
        if (activity.getPersonalRoutine() != null) {
            return UserActivityResponse.fromPersonalActivity(activity);
        } else if (activity.getGroupMember() != null) {
            return UserActivityResponse.fromGroupActivity(activity);
        }
        // This case should not happen with consistent data
        throw new IllegalArgumentException
                ("UserActivity has neither PersonalRoutine nor GroupMember");
    }



    private static final Set<ActivityType> ATTENDANCE_TYPES =
            EnumSet.of(ActivityType.PERSONAL_ROUTINE_COMPLETE, ActivityType.GROUP_AUTH_COMPLETE);

    @Override
    @Transactional(readOnly = true)
    public boolean hasAttendanceOn(Long userId, LocalDate date) {
        return userActivityRepository.existsByUserIdAndActivityDateAndActivityTypeIn(
                userId, date, ATTENDANCE_TYPES
        );
    }


    //연속출석부분
    @Override
    @Transactional(readOnly = true)
    public int getConsecutiveAttendanceDays(Long userId, LocalDate today) {
        // today가 null이면 KST 기준 오늘로
        LocalDate base = (today != null) ? today : LocalDate.now(ZoneId.of("Asia/Seoul"));

        int streak = 0;
        LocalDate cursor = base;

        while (true) {
            boolean attended = userActivityRepository
                    .existsByUserIdAndActivityDateAndActivityTypeIn(
                            userId, cursor, ATTENDANCE_TYPES.stream().toList()
                    );
            if (!attended) break;

            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
