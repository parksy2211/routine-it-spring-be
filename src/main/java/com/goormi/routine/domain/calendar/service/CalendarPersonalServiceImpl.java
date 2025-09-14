package com.goormi.routine.domain.calendar.service;

import com.goormi.routine.domain.calendar.client.KakaoCalendarClient;
import com.goormi.routine.domain.calendar.exception.KakaoApiException;
import com.goormi.routine.domain.calendar.repository.CalendarRepository;
import com.goormi.routine.domain.personal_routines.domain.PersonalRoutine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;

import static com.goormi.routine.domain.calendar.dto.KakaoCalendarDto.*;

/**
 * 캘린더 서비스 개인 루틴용 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "calendar.integration.enabled", havingValue = "true", matchIfMissing = true)
@Transactional(readOnly = true)
public class CalendarPersonalServiceImpl implements CalendarPersonalService {

    private final CalendarRepository calendarRepository;
    private final KakaoCalendarClient kakaoCalendarClient;
    private final KakaoTokenService kakaoTokenService;

    /**
     * 개인 일정 생성
     */

    @Override
    @Transactional
    public String createPersonalSchedule(Long userId, PersonalRoutine personalRoutine) {
        log.info("그룹 일정 생성 시작: userId={}, routineId={}", userId, personalRoutine.getRoutineId());

        try {
            // 카카오 액세스 토큰 획득
            String accessToken = kakaoTokenService.getKakaoAccessTokenByUserId(userId);

            // 그룹 정보를 바탕으로 일정 생성
            CreateEventRequest request = buildEventRequest("primary", personalRoutine);
            CreateEventResponse response = kakaoCalendarClient.createEvent(accessToken, "primary", request);

            log.info("개인 일정 생성 완료: userId={}, eventId={}", userId, response.eventId());

            return response.eventId();

        } catch (Exception e) {
            log.error("개인 일정 생성 실패: userId={}, RoutineId={}", userId, personalRoutine.getRoutineId(), e);
            throw new KakaoApiException("개인 일정 생성에 실패했습니다", e, 500, "EVENT_CREATE_FAILED");
        }
    }

    /**
     * 개인 일정 수정
     */
    @Override
    @Transactional
    public void updatePersonalSchedule(Long userId, PersonalRoutine personalRoutine, String eventId) {
        log.info("개인 일정 수정 시작: userId={}, eventId={}", userId, eventId);

        // 입력 매개변수 유효성 검사
        if (eventId == null || eventId.trim().isEmpty()) {
            log.error("eventId가 null이거나 비어있습니다: '{}'", eventId);
            throw new IllegalArgumentException("eventId는 필수값입니다");
        }

        if (personalRoutine == null) {
            log.error("personalRoutine 객체가 null입니다");
            throw new IllegalArgumentException("personalRoutine 객체는 필수값입니다");
        }

        log.debug("입력 검증 완료 - eventId: '{}', RoutineName: '{}'", eventId, personalRoutine.getRoutineName());

        try {
            // 카카오 액세스 토큰 획득
            String accessToken = kakaoTokenService.getKakaoAccessTokenByUserId(userId);
            log.debug("액세스 토큰 획득 완료");

            GetEventsResponse events = getPersonalEvents(userId);
            String actualEventId = eventId;
            if (events != null && events.events() != null){
                actualEventId = Arrays.stream(events.events())
                        .map(EventBrief::id)
                        .filter(id -> id !=null && id.startsWith(eventId))
                        .findFirst()
                        .orElse(eventId); // null 대신 기본 eventId를 사용하도록 수정
            }
            String calendarId = "primary";
            UpdateEventRequest request = buildUpdateEventRequest(personalRoutine, actualEventId, calendarId);
            log.debug("일정 수정 요청 생성 완료: actualEventId={}, calendarId={}", actualEventId, calendarId);

            // 카카오 API 호출 전 최종 검증 로그
            log.info("카카오 API 호출 시작 - actualEventId: '{}', calendarId: '{}', userId: {}",
                    actualEventId, calendarId, userId);

            try {
                kakaoCalendarClient.updateEvent(accessToken, actualEventId, request);
                log.info("그룹 일정 수정 완료: userId={}, eventId={}", actualEventId, eventId);

            } catch (RuntimeException kakaoApiException) {
                log.error("카카오 API 오류 발생: actualEventId={}, calendarId={}, error={}",
                        actualEventId, calendarId, kakaoApiException.getMessage());

                // 카카오 API에서 이벤트나 캘린더를 찾을 수 없는 경우
                if (kakaoApiException.getMessage().contains("Invalid calendar_id or event_id") ||
                        kakaoApiException.getMessage().contains("not found") ||
                        kakaoApiException.getMessage().contains("400")) {

                    log.error("이벤트를 찾을 수 없음: actualEventId={}, calendarId={}, 상세오류={}",
                            actualEventId, calendarId, kakaoApiException.getMessage());

                    // 일정 수정 실패 시 새 일정 생성하지 않고 오류 전파
                    throw new KakaoApiException(
                            "해당 이벤트를 찾을 수 없습니다. actualEventId: " + actualEventId + ", calendarId: " + calendarId,
                            kakaoApiException, 404, "EVENT_NOT_FOUND");
                } else {
                    throw kakaoApiException; // 다른 오류는 그대로 전파
                }
            }

        } catch (RuntimeException e) {
            // 우리가 던진 검증 오류인지 카카오 API 오류인지 구분
            if (e.getMessage().contains("권한이 없습니다") || e.getMessage().contains("연동되어 있지 않습니다")) {
                log.error("권한/연동 오류: {}", e.getMessage());
                throw e; // 그대로 다시 던짐
            } else {
                log.error("카카오 API 호출 중 오류 발생: userId={}, eventId={}, error={}",
                        userId, eventId, e.getMessage(), e);
                throw new KakaoApiException("카카오 캘린더 API 호출에 실패했습니다: " + e.getMessage(),
                        e, 500, "KAKAO_API_ERROR");
            }
        } catch (Exception e) {
            log.error("개인 일정 수정 실패: userId={}, eventId={}", userId, eventId, e);
            throw new KakaoApiException("개인 일정 수정에 실패했습니다", e, 500, "EVENT_UPDATE_FAILED");
        }
    }

    /**
     * 개인 일정 삭제
     */
    @Override
    @Transactional
    public void deletePersonalSchedule(String eventId, Long userId) {
        log.info("그룹 일정 삭제 시작: eventId={}, userId={}", eventId, userId);

        try {
            // 카카오 액세스 토큰 획득
            String accessToken = kakaoTokenService.getKakaoAccessTokenByUserId(userId);

            GetEventsResponse events = getPersonalEvents(userId);
            String actualEventId = eventId;
            if (events != null && events.events() != null){
                actualEventId = Arrays.stream(events.events())
                        .map(EventBrief::id)
                        .filter(id -> id !=null && id.startsWith(eventId))
                        .findFirst()
                        .orElse(eventId); // null 대신 기본 eventId를 사용하도록 수정
            }

            // 삭제 요청 DTO 생성 (모든 반복 일정 삭제)
            DeleteEventRequest request = DeleteEventRequest.builder()
                    .eventId(actualEventId)
                    .recurUpdateType("ALL")
                    .build();

            kakaoCalendarClient.deleteEvent(accessToken, request);
            log.info("그룹 일정 삭제 완료: actualEventId={}", actualEventId);

        } catch (Exception e) {
            log.error("그룹 일정 삭제 실패: actualEventId={}", eventId, e);
            throw new KakaoApiException("그룹 일정 삭제에 실패했습니다", e, 500, "EVENT_DELETE_FAILED");
        }
    }

    /**
     * 캘린더 내의 이벤트 조회
     */
    @Override
    public GetEventsResponse getPersonalEvents(Long userId) {
        try {
            String accessToken = kakaoTokenService.getKakaoAccessTokenByUserId(userId);
            log.debug("액세스 토큰 획득 완료");

            // 카카오 API는 UTC 기준의 RFC3339 형식을 요구합니다.
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

            // 현재 시각과 8일 후 시각을 UTC 기준으로 포맷팅합니다.
            String from = formatter.format(Instant.now());
            String to = formatter.format(Instant.now().plus(8, ChronoUnit.DAYS));

            GetEventsRequest request = GetEventsRequest.builder()
                    .calendarId("primary")
                    .from(from)
                    .to(to)
                    .build();

            GetEventsResponse response = kakaoCalendarClient.getEvents(accessToken, request);
            return response;
        }  catch (Exception e) {
            throw new KakaoApiException("카카오 일정 조회에 실패했습니다.", e, 500, "GET_EVENTS_FAILED");
        }
    }

    /**
     * 개인 정보를 바탕으로 일정 생성 요청 빌드
     */
    private CreateEventRequest buildEventRequest(String subCalendarId, PersonalRoutine personalRoutine) {
        log.debug("그룹 정보 확인:");
        log.debug("- groupName: {}", personalRoutine.getRoutineName());
        log.debug("- description: {}", personalRoutine.getDescription());
        log.debug("- alarmTime: {}", personalRoutine.getStartTime());
        log.debug("- authDays: {}", personalRoutine.getRepeatDays());

        String startTime = formatAlarmTime(personalRoutine.getStartTime());
        String endTime = formatAlarmTime(personalRoutine.getStartTime().plusMinutes(30)); // 30분 일정으로 설정
        String recurRule = buildRecurRule(personalRoutine.getRepeatDays());

        log.debug("생성된 값들:");
        log.debug("- startTime: {}", startTime);
        log.debug("- endTime: {}", endTime);
        log.debug("- recurRule: {}", recurRule);

        // EventTime 객체 생성
        Time time = Time.builder()
                .startAt(startTime)
                .endAt(endTime)
                .build();

        log.debug("EventTime 생성: {}", time);

        log.debug("일정 생성 요청 빌드:");
        log.debug("- subCalendarId: {}", subCalendarId);
        log.debug("- title: {}", personalRoutine.getRoutineName());
        log.debug("- description: {}", personalRoutine.getDescription());
        log.debug("- time.start_at: {}", startTime);
        log.debug("- time.end_at: {}", endTime);
        log.debug("- startTime: {}", startTime);
        log.debug("- endTime: {}", endTime);
        log.debug("- recurRule: {}", recurRule);
        log.debug("- authDays: {}", personalRoutine.getRepeatDays());
        Integer[] reminders = new Integer[]{5,5};
        EventCreate eventCreate = EventCreate.builder()
                .title(personalRoutine.getRoutineName())
                .description(personalRoutine.getDescription())
                .time(time)  // EventTime 객체 설정
                .rrule(recurRule)
                .reminders(reminders)
                .build();

        log.debug("EventCreate 객체 생성:");
        log.debug("- title: {}", eventCreate.title());
        log.debug("- description: {}", eventCreate.description());
        log.debug("- time: {}", eventCreate.time());
        log.debug("- rrule: {}", eventCreate.rrule());

        CreateEventRequest createEventRequest = CreateEventRequest.builder()
                .calendarId(subCalendarId)
                .event(eventCreate)
                .build();

        log.debug("CreateEventRequest 생성 완료: event={}", createEventRequest.event());

        return createEventRequest;
    }

    /**
     * 개인 정보를 바탕으로 일정 수정 요청 빌드
     */
    private UpdateEventRequest buildUpdateEventRequest(PersonalRoutine personalRoutine, String eventId, String calendarId) {
        log.debug("UpdateEventRequest 빌드 시작: groupName={}, eventId={}, calendarId={}",
                personalRoutine.getRoutineName(), eventId, calendarId);

        String startTime = formatAlarmTime(personalRoutine.getStartTime());
        String endTime = formatAlarmTime(personalRoutine.getStartTime().plusMinutes(30));
        String recurRule = buildRecurRule(personalRoutine.getRepeatDays());

        log.debug("시간 정보 생성:");
        log.debug("- startTime: {}", startTime);
        log.debug("- endTime: {}", endTime);
        log.debug("- recurRule: {}", recurRule);

        Time time = Time.builder()
                .startAt(startTime)
                .endAt(endTime)
                .build();

        EventUpdate eventUpdate = EventUpdate.builder()
                .title(personalRoutine.getRoutineName())
                .description(personalRoutine.getDescription())
                .time(time)
                .rrule(recurRule)
                .build();

        log.debug("EventUpdate 생성 완료:");
        log.debug("- title: {}", eventUpdate.title());
        log.debug("- description: {}", eventUpdate.description());
        log.debug("- rrule: {}", eventUpdate.rrule());
        log.debug("- time: startAt={}, endAt={}",
                eventUpdate.time() != null ? eventUpdate.time().startAt() : "null",
                eventUpdate.time() != null ? eventUpdate.time().endAt() : "null");

        UpdateEventRequest request = UpdateEventRequest.builder()
                .event(eventUpdate)
                .eventId(eventId)
                .calendarId(calendarId)
                .recurUpdateType("THIS_AND_FOLLOWING")
                .build();

        log.debug("UpdateEventRequest 생성 완료: eventId={}, calendarId={}, recurUpdateType={}",
                request.eventId(), request.calendarId(), request.recurUpdateType());

        return request;
    }

    /**
     * 시간 포매팅 헬퍼 메서드 - ISO 8601 형식으로 변환
     */
    private String formatAlarmTime(LocalTime time) {
        // 다음 주 일요일부터 시작하도록 설정 (반복 일정의 시작점)
        LocalDate startDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        LocalDateTime dateTime = LocalDateTime.of(startDate, time);

        // 한국 시간대로 ISO 8601 형식 생성
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of("Asia/Seoul"));
        String formattedTime = zonedDateTime.format(DateTimeFormatter.ISO_INSTANT);

        log.debug("시간 포매팅: LocalTime={} -> ZonedDateTime={} -> ISO={}",
                time, zonedDateTime, formattedTime);

        return formattedTime;
    }

    /**
     * 반복 규칙 생성 헬퍼 메서드
     * authDays 형식: "0101010" (일월화수목금토)
     * 오늘부터 3개월까지 반복 제한
     */
    private String buildRecurRule(String authDays) {
        log.debug("RRULE 생성 시작: authDays={}", authDays);

        // 카카오 캘린더 API 반복 규칙에 맞게 변환
        StringBuilder rule = new StringBuilder("FREQ=WEEKLY;BYDAY=");
        String[] days = {"SU", "MO", "TU", "WE", "TH", "FR", "SA"};

        boolean hasAnyDay = false;
        for (int i = 0; i < authDays.length() && i < 7; i++) {
            if (authDays.charAt(i) == '1') {
                if (hasAnyDay) {
                    rule.append(",");
                }
                rule.append(days[i]);
                hasAnyDay = true;
                log.debug("요일 추가: {} (index={})", days[i], i);
            }
        }

        // 만약 어떤 요일도 선택되지 않았다면 기본값 설정 (매일)
        if (!hasAnyDay) {
            rule.append("SU,MO,TU,WE,TH,FR,SA");
            log.debug("기본값 설정: 모든 요일");
        }

        // 오늘부터 3개월 후까지 반복 제한 (UNTIL 형식)
        LocalDateTime endDate = LocalDateTime.now().plusMonths(3);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        String untilDate = endDate.atZone(ZoneId.of("Asia/Seoul"))
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(formatter);
        rule.append(";UNTIL=").append(untilDate);

        String result = rule.toString();
        log.debug("생성된 RRULE: {} (untilDate: {})", result, untilDate);
        return result;
    }

}
