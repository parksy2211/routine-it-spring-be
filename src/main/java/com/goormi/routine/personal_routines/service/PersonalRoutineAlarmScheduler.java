// src/main/java/com/goormi/routine/personal_routines/service/PersonalRoutineAlarmScheduler.java
package com.goormi.routine.personal_routines.service;

import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.user.repository.UserRepository;
import com.goormi.routine.kakao.KakaoTalkClient;
import com.goormi.routine.personal_routines.domain.PersonalRoutine;
import com.goormi.routine.personal_routines.repository.PersonalRoutineRepository;
import com.goormi.routine.personal_routines.support.RepeatDaysUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"local", "local-mysql", "ci", "dev", "prod"}) // 필요한 프로필 명시
public class PersonalRoutineAlarmScheduler {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final PersonalRoutineRepository routineRepository;
    private final UserRepository userRepository;
    private final KakaoTalkClient kakaoTalkClient;

    /**
     * 매 30초마다 "현재 분"과 동일한 startTime 루틴을 찾아 발송
     * cron의 zone을 못 쓰는 환경 대비 코드에서 KST 계산
     */
    @Scheduled(fixedDelay = 30000)
    public void sendDueAlarms() {
        LocalTime nowMinute = LocalTime.now(ZONE).withSecond(0).withNano(0);
        LocalDate today = LocalDate.now(ZONE);

        List<PersonalRoutine> due = routineRepository
                .findByIsDeletedFalseAndIsAlarmOnTrueAndStartTime(nowMinute);

        for (PersonalRoutine r : due) {
            // 기간/요일 체크
            if (today.isBefore(r.getStartDate()) || today.isAfter(r.getEndDate())) continue;
            if (!RepeatDaysUtil.matches(today, r.getRepeatDays())) continue;

            userRepository.findById(r.getUserId().longValue()).ifPresentOrElse(user -> {
                String token = user.getKakaoAccessToken();
                if (token == null || token.isBlank()) {
                    log.warn("[ALARM] user {} kakao token empty", user.getId());
                    return;
                }
                String text = "⏰ 루틴잇 알림\n" +
                        "루틴: " + r.getRoutineName() + "\n" +
                        "시간: " + r.getStartTime() + "\n" +
                        "오늘도 화이팅!";

                kakaoTalkClient.sendToMe(token, text);
            }, () -> log.warn("[ALARM] user not found: {}", r.getUserId()));
        }
    }
}
