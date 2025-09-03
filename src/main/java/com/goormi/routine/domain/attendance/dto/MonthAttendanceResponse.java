package com.goormi.routine.domain.attendance.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @NoArgsConstructor @AllArgsConstructor
public class MonthAttendanceResponse {
    private String month;           // "YYYY-MM"
    private List<LocalDate> days;   // 출석한 날짜 목록
}
