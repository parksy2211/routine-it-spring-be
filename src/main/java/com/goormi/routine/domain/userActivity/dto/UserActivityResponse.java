package com.goormi.routine.domain.userActivity.dto;

import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.userActivity.entity.ActivityType;
import com.goormi.routine.domain.userActivity.entity.UserActivity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserActivityResponse {
    private User user;
    private ActivityType activityType;
    private LocalDate activityDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer personalRoutineId;
    private String personalRoutineName;
    private Long groupId;
    private String groupName;

    public static UserActivityResponse from(UserActivity userActivity) {
        return UserActivityResponse.builder()
                .user(userActivity.getUser())
                .activityType(userActivity.getActivityType())
                .activityDate(userActivity.getActivityDate())
                .createdAt(userActivity.getCreatedAt())
                .updatedAt(userActivity.getUpdatedAt())
                .personalRoutineId(userActivity.getPersonalRoutine().getRoutineId())
                .personalRoutineName(userActivity.getPersonalRoutine().getRoutineName())
                .groupId(userActivity.getGroupMember().getGroup().getGroupId())
                .groupName(userActivity.getGroupMember().getGroup().getGroupName())
                .build();
    }
}
