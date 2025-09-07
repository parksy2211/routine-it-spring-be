package com.goormi.routine.domain.attendance.dto;

import lombok.*;
import java.util.List;

@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceDashboardResponse {

    public enum DayState { ATTEND, TODAY, ABSENT, FUTURE }

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DayCell {
        private int day;           // 1~31
        private String isoDate;    // "YYYY-MM-DD"
        private DayState state;    // 칩 상태
        private boolean attended;  // 출석 여부
    }

    private String month;                 // "YYYY-MM"
    private List<DayCell> days;           // 달력 셀
    private int currentStreak;            // 오늘 기준 연속
    private int longestStreak;            // 최대 연속
    private int totalActiveDays;          // 누적 출석일
    private int completedRoutines;        // (추후 연결)
}
