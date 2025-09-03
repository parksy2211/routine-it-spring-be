package com.goormi.routine.domain.attendance.service;

import com.goormi.routine.domain.attendance.dto.*;
import com.goormi.routine.domain.attendance.entity.UserAttendance;
import com.goormi.routine.domain.attendance.repository.UserAttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final UserAttendanceRepository attendanceRepo;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /** 출석 체크(멱등) */
    @Transactional
    public CheckInResponse checkIn(Long userId, String device, String ip) {
        LocalDate today = LocalDate.now(KST);

        // 이미 있으면 멱등 응답
        var existing = attendanceRepo.findByUserIdAndCheckDate(userId, today);
        if (existing.isPresent()) {
            int currentStreak = computeStreak(userId, today);
            int longest = computeLongestStreak(userId);
            return CheckInResponse.of(true, existing.get().getCheckedAt(), currentStreak, longest);
        }

        // 신규 체크
        var saved = attendanceRepo.save(
                UserAttendance.builder()
                        .userId(userId)
                        .checkDate(today)
                        .checkedAt(LocalDateTime.now(KST))
                        .device(device)
                        .ip(ip)
                        .build()
        );

        int currentStreak = computeStreak(userId, today);
        int longest = computeLongestStreak(userId);
        return CheckInResponse.of(true, saved.getCheckedAt(), currentStreak, longest);
    }

    /** 오늘 기준 연속 출석(오늘 포함) */
    @Transactional(readOnly = true)
    public int currentStreakToday(Long userId) {
        return computeStreak(userId, LocalDate.now(KST));
    }

    /** 연속 출석: 오늘부터 어제 방향으로 끊길 때까지 */
    @Transactional(readOnly = true)
    public int computeStreak(Long userId, LocalDate today) {
        int streak = 0;
        LocalDate cursor = today;
        while (attendanceRepo.countByUserIdAndCheckDate(userId, cursor) > 0) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    /** 최대 연속 출석(실시간 계산, 캐시 테이블 없음) */
    @Transactional(readOnly = true)
    public int computeLongestStreak(Long userId) {
        var days = attendanceRepo.findAllDatesByUserId(userId);
        int longest = 0, cur = 0;
        LocalDate prev = null;
        for (LocalDate d : days) {
            if (prev != null && d.minusDays(1).equals(prev)) cur++;
            else cur = 1;
            longest = Math.max(longest, cur);
            prev = d;
        }
        return longest;
    }

    /** 월별 출석 일자 */
    @Transactional(readOnly = true)
    public MonthAttendanceResponse getMonth(Long userId, YearMonth ym) {
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        var list = attendanceRepo.findByUserIdAndCheckDateBetween(userId, start, end);
        var days = list.stream().map(UserAttendance::getCheckDate).toList();
        return new MonthAttendanceResponse(ym.toString(), days);
    }

    /** 대시보드(달력 + 통계) */
    @Transactional(readOnly = true)
    public AttendanceDashboardResponse getDashboard(Long userId, YearMonth ym) {
        LocalDate today = LocalDate.now(KST);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        var monthly = attendanceRepo.findByUserIdAndCheckDateBetween(userId, start, end);
        Set<LocalDate> attended = monthly.stream().map(UserAttendance::getCheckDate).collect(Collectors.toSet());

        List<AttendanceDashboardResponse.DayCell> cells = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            boolean has = attended.contains(d);
            AttendanceDashboardResponse.DayState state;
            if (d.isAfter(today)) state = AttendanceDashboardResponse.DayState.FUTURE;
            else if (d.isEqual(today)) state = AttendanceDashboardResponse.DayState.TODAY;
            else state = has ? AttendanceDashboardResponse.DayState.ATTEND : AttendanceDashboardResponse.DayState.ABSENT;

            cells.add(AttendanceDashboardResponse.DayCell.builder()
                    .day(d.getDayOfMonth())
                    .isoDate(d.toString())
                    .state(state)
                    .attended(has)
                    .build());
        }

        int current = computeStreak(userId, today);
        int longest = computeLongestStreak(userId);
        int totalActiveDays = attendanceRepo.findByUserIdAndCheckDateBetween(userId, LocalDate.of(1970,1,1), today).size();

        return AttendanceDashboardResponse.builder()
                .month(ym.toString())
                .days(cells)
                .currentStreak(current)
                .longestStreak(longest)
                .totalActiveDays(totalActiveDays)
                .completedRoutines(0) // TODO: 루틴 완료 로그 연결 시 교체
                .totalPoints(0L)      // TODO: 포인트 원장 연결 시 교체
                .build();
    }
}
