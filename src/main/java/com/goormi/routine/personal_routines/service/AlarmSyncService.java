// src/main/java/com/goormi/routine/personal_routines/service/AlarmSyncService.java
package com.goormi.routine.personal_routines.service;

import com.goormi.routine.kakao.KakaoCalendarClient;
import com.goormi.routine.kakao.UserKakaoTokenService;
import com.goormi.routine.personal_routines.domain.PersonalRoutine;
import com.goormi.routine.personal_routines.support.RRuleUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmSyncService {

    private final KakaoCalendarClient calendarClient;
    private final UserKakaoTokenService tokenService;

    /**
     * 루틴 신규/수정 시 캘린더 동기화:
     * - isAlarmOn=true → 생성 또는 업데이트
     * - isAlarmOn=false → 이벤트가 있으면 삭제
     */
    public void sync(PersonalRoutine r) {
        try {
            String accessToken = tokenService.getValidAccessToken(r.getUserId());
            if (accessToken == null) {
                log.warn("[AlarmSync] user={} kakao access token not found. skip.", r.getUserId());
                return;
            }

            if (Boolean.TRUE.equals(r.getIsAlarmOn())) {
                String rrule = RRuleUtil.toWeeklyRRule(r.getRepeatDays(), r.getEndDate());
                ZonedDateTime start = RRuleUtil.zdtKst(r.getStartDate(), r.getStartTime());
                // 카카오 이벤트는 end가 start와 같으면 거부될 수 있으므로 5분 뒤로
                ZonedDateTime end = start.plusMinutes(5);

                if (r.getKakaoCalendarEventId() == null) {
                    // 생성
                    String eventId = calendarClient.createEvent(
                            accessToken,
                            r.getRoutineName(),
                            r.getDescription(),
                            start, end,
                            rrule,
                            0 // 0분 전 알림 (정시)
                    );
                    r.setKakaoCalendarEventId(eventId);
                    log.info("[AlarmSync] created event user={} routine={} eventId={}",
                            r.getUserId(), r.getRoutineId(), eventId);
                } else {
                    // 업데이트
                    calendarClient.updateEvent(
                            accessToken,
                            r.getKakaoCalendarEventId(),
                            r.getRoutineName(),
                            r.getDescription(),
                            start, end,
                            rrule,
                            0
                    );
                    log.info("[AlarmSync] updated event user={} routine={} eventId={}",
                            r.getUserId(), r.getRoutineId(), r.getKakaoCalendarEventId());
                }
            } else {
                // 알람 OFF → 이벤트 삭제
                if (r.getKakaoCalendarEventId() != null) {
                    calendarClient.deleteEvent(accessToken, r.getKakaoCalendarEventId());
                    log.info("[AlarmSync] deleted event user={} routine={} eventId={}",
                            r.getUserId(), r.getRoutineId(), r.getKakaoCalendarEventId());
                    r.setKakaoCalendarEventId(null);
                }
            }
        } catch (Exception e) {
            // 메인 트랜잭션을 깨지 않도록 로깅 후 패스 (운영에서는 Sentry 등 알림)
            log.error("[AlarmSync] sync failed routineId=" + r.getRoutineId(), e);
        }
    }

    /** 엔티티 삭제 시 호출 */
    public void deleteIfExists(PersonalRoutine r) {
        try {
            if (r.getKakaoCalendarEventId() == null) return;
            String token = tokenService.getValidAccessToken(r.getUserId());
            if (token == null) return;
            calendarClient.deleteEvent(token, r.getKakaoCalendarEventId());
            log.info("[AlarmSync] delete on entity removal routine={} eventId={}",
                    r.getRoutineId(), r.getKakaoCalendarEventId());
            r.setKakaoCalendarEventId(null);
        } catch (Exception e) {
            log.error("[AlarmSync] delete failed routineId=" + r.getRoutineId(), e);
        }
    }
}
