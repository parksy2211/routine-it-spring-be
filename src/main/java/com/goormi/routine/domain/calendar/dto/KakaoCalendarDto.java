package com.goormi.routine.domain.calendar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * 카카오 캘린더 API 요청/응답을 위한 DTO들
 */
public class KakaoCalendarDto {

    /**
     * 서브캘린더 생성 요청 DTO
     */
    @Builder
    public record CreateSubCalendarRequest(
            String name,
            String color,
            @JsonProperty("reminder") Integer reminderMinutes
    ) {}

    /**
     * 서브캘린더 생성 응답 DTO
     */
    public record CreateSubCalendarResponse(
            @JsonProperty("calendar_id") String subCalendarId,
            String name,
            String color,
            Integer reminder
    ) {}

    /**
     * 일정 생성 요청 DTO
     */
    @Builder
    public record CreateEventRequest(
            @JsonProperty("sub_calendar_id") String subCalendarId,
            String title,
            String description,
            @JsonProperty("start_time") String startTime,
            @JsonProperty("end_time") String endTime,
            @JsonProperty("recur_rule") String recurRule,
            @JsonProperty("alarm_time") Integer alarmTime
    ) {}

    /**
     * 일정 생성 응답 DTO
     */
    public record CreateEventResponse(
            @JsonProperty("event_id") String eventId,
            String title,
            String description,
            @JsonProperty("start_time") String startTime,
            @JsonProperty("end_time") String endTime
    ) {}

    /**
     * 일정 수정 요청 DTO
     */
    @Builder
    public record UpdateEventRequest(
            String title,
            String description,
            @JsonProperty("start_time") String startTime,
            @JsonProperty("end_time") String endTime,
            @JsonProperty("recur_rule") String recurRule,
            @JsonProperty("recur_update_type") String recurUpdateType,
            @JsonProperty("alarm_time") Integer alarmTime
    ) {}

    /**
     * 카카오 API 에러 응답 DTO
     */
    public record KakaoErrorResponse(
            Integer code,
            String msg
    ) {}
}
