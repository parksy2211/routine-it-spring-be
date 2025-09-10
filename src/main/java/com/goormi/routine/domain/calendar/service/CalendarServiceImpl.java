package com.goormi.routine.domain.calendar.service;

import com.goormi.routine.domain.calendar.client.KakaoCalendarClient;
import com.goormi.routine.domain.calendar.dto.CalendarResponse;
import com.goormi.routine.domain.calendar.dto.KakaoCalendarDto.*;
import com.goormi.routine.domain.calendar.entity.UserCalendar;
import com.goormi.routine.domain.calendar.exception.CalendarAlreadyConnectedException;
import com.goormi.routine.domain.calendar.exception.CalendarNotFoundException;
import com.goormi.routine.domain.calendar.exception.KakaoApiException;
import com.goormi.routine.domain.calendar.repository.CalendarRepository;
import com.goormi.routine.domain.group.entity.Group;
import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 캘린더 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "calendar.integration.enabled", havingValue = "true", matchIfMissing = true)
@Transactional(readOnly = true)
public class CalendarServiceImpl implements CalendarService {

    private final CalendarRepository calendarRepository;
    private final UserRepository userRepository;
    private final KakaoCalendarClient kakaoCalendarClient;
    private final KakaoTokenService kakaoTokenService;

    /**
     * 사용자 캘린더 생성
     */
    @Override
    @Transactional
    public CalendarResponse createUserCalendar(Long userId) {
        log.info("사용자 캘린더 생성 시작: userId={}", userId);
        
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        
        // 이미 캘린더가 있는지 확인
        if (calendarRepository.existsByUser(user)) {
            throw new CalendarAlreadyConnectedException("이미 캘린더가 연동되어 있습니다: " + userId);
        }
        
        try {
            // 카카오 액세스 토큰 획득
            String accessToken = kakaoTokenService.getKakaoAccessTokenByUserId(userId);
            // 카카오 서브캘린더 생성
            CreateSubCalendarRequest request = CreateSubCalendarRequest.builder()
                    .name("routine-it for group")
                    .color("LIME")
                    .reminderMinutes(10)
                    .build();
            
            CreateSubCalendarResponse kakaoResponse = kakaoCalendarClient.createSubCalendar(accessToken, request);
            
            // UserCalendar 엔티티 생성 및 저장
            UserCalendar userCalendar = UserCalendar.createUserCalendar(user, kakaoResponse.subCalendarId());
            UserCalendar savedCalendar = calendarRepository.save(userCalendar);
            
            // User 엔티티의 캘린더 연동 상태 업데이트 (변경 감지 활용)
            user.connectCalendar();
            
            log.info("사용자 캘린더 생성 완료: userId={}, subCalendarId={}", userId, kakaoResponse.subCalendarId());
            
            return CalendarResponse.from(savedCalendar);
            
        } catch (Exception e) {
            log.error("캘린더 생성 실패: userId={}", userId, e);
            throw new KakaoApiException("캘린더 생성에 실패했습니다", e, 500, "CALENDAR_CREATE_FAILED");
        }
    }

    /**
     * 사용자 캘린더 삭제
     */
    @Override
    @Transactional
    public void deleteUserCalendar(Long userId) {
        log.info("사용자 캘린더 삭제 시작: userId={}", userId);
        
        UserCalendar userCalendar = calendarRepository.findByUserId(userId)
                .orElse(null);
        
        if (userCalendar == null) {
            log.warn("삭제할 캘린더가 없습니다: userId={}", userId);
            return;
        }
        
        try {
            // 카카오 액세스 토큰 획득
            String accessToken = kakaoTokenService.getKakaoAccessTokenByUserId(userId);
            // 카카오 서브캘린더 삭제
            kakaoCalendarClient.deleteSubCalendar(accessToken, userCalendar.getSubCalendarId());
            
            // UserCalendar 엔티티 삭제
            calendarRepository.delete(userCalendar);
            
            // User 엔티티의 캘린더 연동 상태 업데이트
            User user = userCalendar.getUser();
            user.disconnectCalendar();
            
            log.info("사용자 캘린더 삭제 완료: userId={}, subCalendarId={}", userId, userCalendar.getSubCalendarId());
            
        } catch (Exception e) {
            log.error("캘린더 삭제 실패: userId={}", userId, e);
            // 카카오 API 실패해도 로컬 데이터는 정리
            calendarRepository.delete(userCalendar);
            userCalendar.getUser().disconnectCalendar();
        }
    }

    /**
     * 그룹 일정 생성
     */
    @Override
    @Transactional
    public String createGroupSchedule(Long userId, Group group) {
        log.info("그룹 일정 생성 시작: userId={}, groupId={}", userId, group.getGroupId());
        
        UserCalendar userCalendar = calendarRepository.findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new CalendarNotFoundException("활성화된 캘린더를 찾을 수 없습니다: " + userId));
        
        try {
            // 카카오 액세스 토큰 획득
            String accessToken = kakaoTokenService.getKakaoAccessTokenByUserId(userId);
            
            // 그룹 정보를 바탕으로 일정 생성
            CreateEventRequest request = buildEventRequest(userCalendar.getSubCalendarId(), group);
            CreateEventResponse response = kakaoCalendarClient.createEvent(accessToken, request);
            
            log.info("그룹 일정 생성 완료: userId={}, eventId={}", userId, response.eventId());
            
            return response.eventId();
            
        } catch (Exception e) {
            log.error("그룹 일정 생성 실패: userId={}, groupId={}", userId, group.getGroupId(), e);
            throw new KakaoApiException("그룹 일정 생성에 실패했습니다", e, 500, "EVENT_CREATE_FAILED");
        }
    }

    /**
     * 그룹 일정 수정
     */
    @Override
    @Transactional
    public void updateGroupSchedule(Long userId, Group group, String eventId) {
        log.info("그룹 일정 수정 시작: userId={}, eventId={}", userId, eventId);
        
        try {
            // 카카오 액세스 토큰 획득
            String accessToken = kakaoTokenService.getKakaoAccessTokenByUserId(userId);
            
            UpdateEventRequest request = buildUpdateEventRequest(group);
            kakaoCalendarClient.updateEvent(accessToken, eventId, request);
            
            log.info("그룹 일정 수정 완료: userId={}, eventId={}", userId, eventId);
            
        } catch (Exception e) {
            log.error("그룹 일정 수정 실패: userId={}, eventId={}", userId, eventId, e);
            throw new KakaoApiException("그룹 일정 수정에 실패했습니다", e, 500, "EVENT_UPDATE_FAILED");
        }
    }

    /**
     * 그룹 일정 삭제
     */
    @Override
    @Transactional
    public void deleteGroupSchedule(String eventId, Long userId) {
        log.info("그룹 일정 삭제 시작: eventId={}, userId={}", eventId, userId);
        
        try {
            // 카카오 액세스 토큰 획득
            String accessToken = kakaoTokenService.getKakaoAccessTokenByUserId(userId);
            
            kakaoCalendarClient.deleteEvent(accessToken, eventId);
            log.info("그룹 일정 삭제 완료: eventId={}", eventId);
            
        } catch (Exception e) {
            log.error("그룹 일정 삭제 실패: eventId={}", eventId, e);
            throw new KakaoApiException("그룹 일정 삭제에 실패했습니다", e, 500, "EVENT_DELETE_FAILED");
        }
    }

    /**
     * 사용자 캘린더 조회
     */
    @Override
    public CalendarResponse getUserCalendar(Long userId) {
        UserCalendar userCalendar = calendarRepository.findByUserId(userId)
                .orElseThrow(() -> new CalendarNotFoundException("캘린더를 찾을 수 없습니다: " + userId));
        
        return CalendarResponse.from(userCalendar);
    }

    /**
     * 캘린더 연동 상태 확인
     */
    @Override
    public boolean isCalendarConnected(Long userId) {
        return calendarRepository.existsByUserIdAndActiveTrue(userId);
    }

    /**
     * 그룹 정보를 바탕으로 일정 생성 요청 빌드
     */
    private CreateEventRequest buildEventRequest(String subCalendarId, Group group) {
        String startTime = formatAlarmTime(group.getAlarmTime());
        String endTime = formatAlarmTime(group.getAlarmTime().plusMinutes(30)); // 30분 일정으로 설정
        String recurRule = buildRecurRule(group.getAuthDays());
        
        return CreateEventRequest.builder()
                .subCalendarId(subCalendarId)
                .title(group.getGroupName())
                .description(group.getDescription())
                .startTime(startTime)
                .endTime(endTime)
                .recurRule(recurRule)
                .alarmTime(10) // 10분 전 알림
                .build();
    }

    /**
     * 그룹 정보를 바탕으로 일정 수정 요청 빌드
     */
    private UpdateEventRequest buildUpdateEventRequest(Group group) {
        String startTime = formatAlarmTime(group.getAlarmTime());
        String endTime = formatAlarmTime(group.getAlarmTime().plusMinutes(30));
        String recurRule = buildRecurRule(group.getAuthDays());
        
        return UpdateEventRequest.builder()
                .title(group.getGroupName())
                .description(group.getDescription())
                .startTime(startTime)
                .endTime(endTime)
                .recurRule(recurRule)
                .recurUpdateType("THIS_AND_FOLLOWING")
                .alarmTime(10)
                .build();
    }

    /**
     * 시간 포매팅 헬퍼 메서드
     */
    private String formatAlarmTime(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * 반복 규칙 생성 헬퍼 메서드
     * authDays 형식: "0101010" (일월화수목금토)
     */
    private String buildRecurRule(String authDays) {
        // 카카오 캘린더 API 반복 규칙에 맞게 변환
        StringBuilder rule = new StringBuilder("FREQ=WEEKLY;BYDAY=");
        String[] days = {"SU", "MO", "TU", "WE", "TH", "FR", "SA"};
        
        for (int i = 0; i < authDays.length() && i < 7; i++) {
            if (authDays.charAt(i) == '1') {
                if (rule.length() > "FREQ=WEEKLY;BYDAY=".length()) {
                    rule.append(",");
                }
                rule.append(days[i]);
            }
        }
        
        return rule.toString();
    }
}
