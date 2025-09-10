package com.goormi.routine.domain.calendar.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormi.routine.domain.calendar.dto.KakaoCalendarDto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * 카카오 캘린더 API 호출 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "calendar.integration.enabled", havingValue = "true", matchIfMissing = true)
public class KakaoCalendarClient {

    private final WebClient webClient;

    @Value("${kakao.calendar.base-url:https://kapi.kakao.com/v2/api/calendar}")
    private String baseUrl;

    /**
     * 서브캘린더 생성
     * 
     * @param accessToken 카카오 액세스 토큰
     * @param request 서브캘린더 생성 요청
     * @return 생성된 서브캘린더 정보
     */
    public CreateSubCalendarResponse createSubCalendar(String accessToken, CreateSubCalendarRequest request) {
        log.info("=== 카카오 서브캘린더 생성 API 호출 시작 ===");
        log.debug("Base URL: {}", baseUrl);
        log.debug("카카오 서브캘린더 생성 요청: name={}, color={}", request.name(), request.color());
        log.debug("Access Token 존재 여부: {}", accessToken != null && !accessToken.trim().isEmpty());
        
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("name", request.name());
            formData.add("color", request.color());
            if (request.reminderMinutes() != null) {
                formData.add("reminder", request.reminderMinutes().toString());
            }
            
            return webClient.post()
                    .uri(baseUrl + "/create/calendar")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(CreateSubCalendarResponse.class)
                    .doOnSuccess(response -> log.info("서브캘린더 생성 성공: subCalendarId={}", response.subCalendarId()))
                    .doOnError(error -> log.error("서브캘린더 생성 실패", error))
                    .block();
                    
        } catch (WebClientResponseException e) {
            log.error("카카오 API 호출 오류: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("서브캘린더 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 서브캘린더 삭제
     * 
     * @param accessToken 카카오 액세스 토큰
     * @param subCalendarId 삭제할 서브캘린더 ID
     */
    public void deleteSubCalendar(String accessToken, String subCalendarId) {
        log.info("=== 카카오 서브캘린더 삭제 API 호출 시작 ===");
        log.debug("카카오 서브캘린더 삭제 요청: subCalendarId={}", subCalendarId);
        log.debug("Access Token 존재 여부: {}", accessToken != null && !accessToken.trim().isEmpty());
        
        try {
            webClient.delete()
                    .uri(uriBuilder -> uriBuilder
                            .path(baseUrl + "/delete/calendar")
                            .queryParam("calendar_id", subCalendarId)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(response -> log.info("서브캘린더 삭제 성공: subCalendarId={}", subCalendarId))
                    .doOnError(error -> log.error("서브캘린더 삭제 실패: subCalendarId={}", subCalendarId, error))
                    .block();
                    
        } catch (WebClientResponseException e) {
            log.error("카카오 API 호출 오류: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("서브캘린더 삭제에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 일정 생성
     * 
     * @param accessToken 카카오 액세스 토큰
     * @param request 일정 생성 요청
     * @return 생성된 일정 정보
     */
    public CreateEventResponse createEvent(String accessToken, CreateEventRequest request) {
        log.debug("카카오 일정 생성 요청: title={}, subCalendarId={}", request.title(), request.subCalendarId());
        
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            if (request.subCalendarId() != null) {
                formData.add("calendar_id", request.subCalendarId());
            }
            
            // JSON 형태로 event 데이터 변환 (subCalendarId 제외)
            ObjectMapper objectMapper = new ObjectMapper();
            CreateEventRequest eventData = CreateEventRequest.builder()
                    .title(request.title())
                    .description(request.description())
                    .startTime(request.startTime())
                    .endTime(request.endTime())
                    .recurRule(request.recurRule())
                    .alarmTime(request.alarmTime())
                    .build();
            String eventJson = objectMapper.writeValueAsString(eventData);
            formData.add("event", eventJson);
            
            return webClient.post()
                    .uri(baseUrl + "/create/event")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(CreateEventResponse.class)
                    .doOnSuccess(response -> log.info("일정 생성 성공: eventId={}", response.eventId()))
                    .doOnError(error -> log.error("일정 생성 실패", error))
                    .block();
                    
        } catch (Exception e) {
            log.error("카카오 API 호출 오류", e);
            throw new RuntimeException("일정 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 일정 수정
     * 
     * @param accessToken 카카오 액세스 토큰
     * @param eventId 수정할 일정 ID
     * @param request 일정 수정 요청
     */
    public void updateEvent(String accessToken, String eventId, UpdateEventRequest request) {
        log.debug("카카오 일정 수정 요청: eventId={}, title={}", eventId, request.title());
        
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("event_id", eventId);
            
            // JSON 형태로 event 데이터 변환
            ObjectMapper objectMapper = new ObjectMapper();
            String eventJson = objectMapper.writeValueAsString(request);
            formData.add("event", eventJson);
            
            webClient.post()
                    .uri(baseUrl + "/update/event/host")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(response -> log.info("일정 수정 성공: eventId={}", eventId))
                    .doOnError(error -> log.error("일정 수정 실패: eventId={}", eventId, error))
                    .block();
                    
        } catch (Exception e) {
            log.error("카카오 API 호출 오류: eventId={}", eventId, e);
            throw new RuntimeException("일정 수정에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 일정 삭제
     * 
     * @param accessToken 카카오 액세스 토큰
     * @param eventId 삭제할 일정 ID
     */
    public void deleteEvent(String accessToken, String eventId) {
        log.debug("카카오 일정 삭제 요청: eventId={}", eventId);
        
        try {
            webClient.delete()
                    .uri(uriBuilder -> uriBuilder
                            .path(baseUrl + "/delete/event")
                            .queryParam("event_id", eventId)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(response -> log.info("일정 삭제 성공: eventId={}", eventId))
                    .doOnError(error -> log.error("일정 삭제 실패: eventId={}", eventId, error))
                    .block();
                    
        } catch (WebClientResponseException e) {
            log.error("카카오 API 호출 오류: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("일정 삭제에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 카카오 API 응답 에러 처리를 위한 헬퍼 메서드
     */
    private Mono<String> handleError(WebClientResponseException ex) {
        log.error("카카오 캘린더 API 오류: status={}, message={}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return Mono.error(new RuntimeException("카카오 캘린더 API 호출 실패: " + ex.getMessage()));
    }
}
