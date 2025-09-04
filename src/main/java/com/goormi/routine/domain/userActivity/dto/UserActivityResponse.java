package com.goormi.routine.domain.userActivity.dto;

import com.goormi.routine.domain.userActivity.entity.ActivityType;
import com.goormi.routine.domain.userActivity.entity.UserActivity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserActivityResponse {
    private Long userId;
    private ActivityType activityType;
    private LocalDate activityDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Schema(description = "개인 루틴 ID (Group 활동일 경우 null)")
    private Integer personalRoutineId;
    @Schema(description = "개인 루틴 이름 (Group 활동일 경우 null)")
    private String personalRoutineName;

    @Schema(description = "그룹 ID (PersonalRoutine 활동일 경우 null)")
    private Long groupId;
    @Schema(description = "그룹 이름 (PersonalRoutine 활동일 경우 null)")
    private String groupName;

    public static UserActivityResponse from(UserActivity userActivity) {
        return UserActivityResponse.builder()
                .userId(userActivity.getUser().getId())
                .activityType(userActivity.getActivityType())
                .activityDate(userActivity.getActivityDate())
                .createdAt(userActivity.getCreatedAt())
                .updatedAt(userActivity.getUpdatedAt())
                .personalRoutineId(
                        userActivity.getPersonalRoutine() != null
                                ? userActivity.getPersonalRoutine().getRoutineId()
                                : null)
                .personalRoutineName(
                        userActivity.getPersonalRoutine() != null
                                ? userActivity.getPersonalRoutine().getRoutineName()
                                : null)
                .groupId(
                        userActivity.getGroupMember() != null
                                ? userActivity.getGroupMember().getGroup().getGroupId()
                                : null)
                .groupName(
                        userActivity.getGroupMember() != null
                                ? userActivity.getGroupMember().getGroup().getGroupName()
                                : null)
                .build();
    }
}
