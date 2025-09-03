package com.goormi.routine.domain.attendance.controller;

import com.goormi.routine.common.response.ApiResponse;
import com.goormi.routine.domain.attendance.dto.*;
import com.goormi.routine.domain.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@Tag(name = "출석", description = "출석 체크 및 달력/통계 API")
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "출석 체크(멱등)")
    @PostMapping("/check-in")
    public ApiResponse<CheckInResponse> checkIn(
            @AuthenticationPrincipal Long userId,
            @RequestHeader(value = "X-DEVICE", required = false) String device,
            HttpServletRequest request
    ) {
        return ApiResponse.success(
                attendanceService.checkIn(userId, device, request.getRemoteAddr())
        );
    }

    @Operation(summary = "연속 출석 조회(오늘 기준)")
    @GetMapping("/streak")
    public ApiResponse<?> streak(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success(
                java.util.Map.of("currentStreak", attendanceService.currentStreakToday(userId))
        );
    }

    @Operation(summary = "월별 출석 일자")
    @GetMapping("/month")
    public ApiResponse<MonthAttendanceResponse> month(
            @AuthenticationPrincipal Long userId,
            @RequestParam String yearMonth   // "YYYY-MM"
    ) {
        return ApiResponse.success(
                attendanceService.getMonth(userId, YearMonth.parse(yearMonth))
        );
    }

    @Operation(summary = "대시보드(달력+통계)")
    @GetMapping("/dashboard")
    public ApiResponse<AttendanceDashboardResponse> dashboard(
            @AuthenticationPrincipal Long userId,
            @RequestParam String yearMonth   // "YYYY-MM"
    ) {
        return ApiResponse.success(
                attendanceService.getDashboard(userId, YearMonth.parse(yearMonth))
        );
    }
}