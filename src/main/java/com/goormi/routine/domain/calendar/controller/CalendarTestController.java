package com.goormi.routine.domain.calendar.controller;

import com.goormi.routine.domain.calendar.client.KakaoCalendarClient;
import com.goormi.routine.domain.calendar.dto.KakaoCalendarDto.*;
import com.goormi.routine.domain.calendar.service.KakaoTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 카카오 캘린더 API 테스트용 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/test/calendar")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "calendar.integration.enabled", havingValue = "true", matchIfMissing = true)
public class CalendarTestController {

    private final KakaoCalendarClient kakaoCalendarClient;
    private final KakaoTokenService kakaoTokenService;

    /**
     * 특정 캘린더의 일정 조회 (기본: 오늘부터 7일간)
     * 
     * GET /api/test/calendar/events/{userId}?calendarId={calendarId}&days={days}&limit={limit}
     */
    @GetMapping("/events/{userId}")
    public ResponseEntity<getEventsResponse> getEvents(
            @PathVariable Long userId,
            @RequestParam String calendarId,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(required = false) Integer limit) {
        
        log.info("일정 조회 테스트 요청: userId={}, calendarId={}, days={}, limit={}", 
                userId, calendarId, days, limit);
        
        try {
            // 카카오 액세스 토큰 획득
            String accessToken = kakaoTokenService.getKakaoAccessTokenByUserId(userId);
            log.debug("액세스 토큰 획득 완료");
            
            // 조회 기간 설정 (오늘부터 지정된 일수까지)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime until = now.plusDays(days);
            
            // ISO 8601 형식으로 변환
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
            String from = now.atZone(ZoneId.of("Asia/Seoul")).format(formatter);
            String to = until.atZone(ZoneId.of("Asia/Seoul")).format(formatter);
            
            log.debug("조회 기간: from={}, to={}", from, to);
            
            // 카카오 API 호출
            getEventsResponse response = kakaoCalendarClient.getEvents(
                    accessToken, calendarId, from, to, limit);
            
            log.info("일정 조회 완료: 조회된 일정 수={}", 
                    response.events() != null ? response.events().length : 0);
            
            // 조회된 일정 상세 로깅
            if (response.events() != null) {
                for (EventBrief event : response.events()) {
                    log.info("일정 정보: id={}, title={}, calendarId={}", 
                            event.id(), event.title(), event.calendarId());
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("일정 조회 테스트 실패: userId={}, calendarId={}", userId, calendarId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 기간의 일정 조회 (사용자 정의 기간)
     * 
     * GET /api/test/calendar/events/{userId}/range?calendarId={calendarId}&from={from}&to={to}&limit={limit}
     */
    @GetMapping("/events/{userId}/range")
    public ResponseEntity<getEventsResponse> getEventsByRange(
            @PathVariable Long userId,
            @RequestParam String calendarId,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) Integer limit) {
        
        log.info("기간별 일정 조회 테스트 요청: userId={}, calendarId={}, from={}, to={}, limit={}", 
                userId, calendarId, from, to, limit);
        
        try {
            // 카카오 액세스 토큰 획득
            String accessToken = kakaoTokenService.getKakaoAccessTokenByUserId(userId);
            log.debug("액세스 토큰 획득 완료");
            
            // 카카오 API 호출
            getEventsResponse response = kakaoCalendarClient.getEvents(
                    accessToken, calendarId, from, to, limit);
            
            log.info("기간별 일정 조회 완료: 조회된 일정 수={}", 
                    response.events() != null ? response.events().length : 0);
            
            // 조회된 일정 상세 로깅
            if (response.events() != null) {
                for (EventBrief event : response.events()) {
                    log.info("일정 정보: id={}, title={}, calendarId={}", 
                            event.id(), event.title(), event.calendarId());
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("기간별 일정 조회 테스트 실패: userId={}, calendarId={}, from={}, to={}", 
                    userId, calendarId, from, to, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 일정의 상세 정보 조회 (eventId로 조회)
     * 
     * GET /api/test/calendar/events/{userId}/search?calendarId={calendarId}&eventId={eventId}
     */
    @GetMapping("/events/{userId}/search")
    public ResponseEntity<EventBrief> findEventById(
            @PathVariable Long userId,
            @RequestParam String calendarId,
            @RequestParam String eventId) {
        
        log.info("일정 검색 테스트 요청: userId={}, calendarId={}, eventId={}", 
                userId, calendarId, eventId);
        
        try {
            // 넓은 범위로 일정 조회 (최근 1개월)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime past = now.minusMonths(1);
            LocalDateTime future = now.plusMonths(1);
            
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
            String from = past.atZone(ZoneId.of("Asia/Seoul")).format(formatter);
            String to = future.atZone(ZoneId.of("Asia/Seoul")).format(formatter);
            
            // 카카오 액세스 토큰 획득
            String accessToken = kakaoTokenService.getKakaoAccessTokenByUserId(userId);
            
            // 카카오 API 호출 (큰 범위로 조회)
            getEventsResponse response = kakaoCalendarClient.getEvents(
                    accessToken, calendarId, from, to, 100);
            
            // eventId로 특정 일정 찾기
            if (response.events() != null) {
                for (EventBrief event : response.events()) {
                    if (eventId.equals(event.id())) {
                        log.info("일정 찾기 성공: id={}, title={}", event.id(), event.title());
                        return ResponseEntity.ok(event);
                    }
                }
            }
            
            log.warn("해당 eventId를 가진 일정을 찾을 수 없습니다: {}", eventId);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("일정 검색 테스트 실패: userId={}, eventId={}", userId, eventId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 테스트용 간단한 일정 조회 (오늘만)
     * 
     * GET /api/test/calendar/events/{userId}/today?calendarId={calendarId}
     */
    @GetMapping("/events/{userId}/today")
    public ResponseEntity<getEventsResponse> getTodayEvents(
            @PathVariable Long userId,
            @RequestParam String calendarId) {
        
        log.info("오늘 일정 조회 테스트 요청: userId={}, calendarId={}", userId, calendarId);
        
        try {
            // 오늘 하루만 조회
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
            
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
            String from = startOfDay.atZone(ZoneId.of("Asia/Seoul")).format(formatter);
            String to = endOfDay.atZone(ZoneId.of("Asia/Seoul")).format(formatter);
            
            // 카카오 액세스 토큰 획득
            String accessToken = kakaoTokenService.getKakaoAccessTokenByUserId(userId);
            
            // 카카오 API 호출
            getEventsResponse response = kakaoCalendarClient.getEvents(
                    accessToken, calendarId, from, to, null);
            
            log.info("오늘 일정 조회 완료: 조회된 일정 수={}", 
                    response.events() != null ? response.events().length : 0);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("오늘 일정 조회 테스트 실패: userId={}, calendarId={}", userId, calendarId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
