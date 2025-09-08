package com.goormi.routine.domain.userActivity.dto;

import com.goormi.routine.domain.userActivity.entity.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityRequest {
    private Long activityId;
    private ActivityType activityType;
    private LocalDate activityDate;
    private Integer personalRoutineId;
    private Long groupId;
    private String imageUrl;
    private Boolean isPublic;

}
