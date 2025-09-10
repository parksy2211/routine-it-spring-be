package com.goormi.routine.domain.calendar.client;

import com.goormi.routine.domain.calendar.dto.KakaoCalendarDto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * 카카오 캘린더 API 호출 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
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
        log.debug("카카오 서브캘린더 생성 요청: name={}, color={}", request.name(), request.color());
        
        try {
            return webClient.post()
                    .uri(baseUrl + "/sub")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
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
        log.debug("카카오 서브캘린더 삭제 요청: subCalendarId={}", subCalendarId);
        
        try {
            webClient.delete()
                    .uri(baseUrl + "/sub/{subCalendarId}", subCalendarId)
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
            return webClient.post()
                    .uri(baseUrl + "/event")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CreateEventResponse.class)
                    .doOnSuccess(response -> log.info("일정 생성 성공: eventId={}", response.eventId()))
                    .doOnError(error -> log.error("일정 생성 실패", error))
                    .block();
                    
        } catch (WebClientResponseException e) {
            log.error("카카오 API 호출 오류: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
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
            webClient.put()
                    .uri(baseUrl + "/event/{eventId}", eventId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(response -> log.info("일정 수정 성공: eventId={}", eventId))
                    .doOnError(error -> log.error("일정 수정 실패: eventId={}", eventId, error))
                    .block();
                    
        } catch (WebClientResponseException e) {
            log.error("카카오 API 호출 오류: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
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
                    .uri(baseUrl + "/event/{eventId}", eventId)
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
