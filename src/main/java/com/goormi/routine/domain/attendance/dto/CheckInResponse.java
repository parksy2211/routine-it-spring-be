package com.goormi.routine.domain.attendance.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @NoArgsConstructor @AllArgsConstructor
public class CheckInResponse {
    private boolean checked;            // 항상 true (멱등 포함)
    private LocalDateTime checkedAt;    // 최초 또는 기존 체크 시간
    private int currentStreak;          // 오늘 포함 연속
    private int longestStreak;          // 최대 연속(실시간 계산)

    public static CheckInResponse of(boolean checked, LocalDateTime at, int streak, int longest) {
        return new CheckInResponse(checked, at, streak, longest);
    }
}
