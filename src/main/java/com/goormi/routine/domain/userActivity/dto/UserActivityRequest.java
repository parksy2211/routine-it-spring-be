package com.goormi.routine.domain.userActivity.dto;

import com.goormi.routine.domain.userActivity.entity.ActivityType;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "공개 여부 (생성시 기본값 false, 업데이트할때만 사용할 것)")
    private Boolean isPublic;

}
