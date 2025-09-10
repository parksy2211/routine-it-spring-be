package com.goormi.routine.domain.calendar.dto;

import com.goormi.routine.domain.calendar.entity.UserCalendar.CalendarColor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

/**
 * 캘린더 관련 DTO들
 */
public class CalendarRequest {

    /**
     * 캘린더 생성 요청 DTO
     */
    @Builder
    public record CalendarCreateRequest(
            @NotBlank(message = "캘린더 이름은 필수입니다")
            String calendarName,
            
            @NotNull(message = "캘린더 색상은 필수입니다")
            CalendarColor color,
            
            @NotNull(message = "알림 시간은 필수입니다")
            @PositiveOrZero(message = "알림 시간은 0 이상이어야 합니다")
            Integer reminderMinutes
    ) {}

    /**
     * 캘린더 설정 수정 요청 DTO
     */
    @Builder
    public record CalendarUpdateRequest(
            String calendarName,
            CalendarColor color,
            @PositiveOrZero(message = "알림 시간은 0 이상이어야 합니다")
            Integer reminderMinutes
    ) {}

    /**
     * 그룹 일정 생성 요청 DTO
     */
    @Builder
    public record GroupScheduleRequest(
            @NotNull(message = "그룹 ID는 필수입니다")
            Long groupId,
            
            @NotBlank(message = "일정 제목은 필수입니다")
            String title,
            
            String description,
            
            @NotBlank(message = "반복 규칙은 필수입니다")
            String authDays, // "0101010" 형태
            
            String alarmTime
    ) {}
}
