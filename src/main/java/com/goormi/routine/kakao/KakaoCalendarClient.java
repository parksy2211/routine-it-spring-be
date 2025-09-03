// src/main/java/com/goormi/routine/integration/kakao/KakaoCalendarClient.java
package com.goormi.routine.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class KakaoCalendarClient {

    private final WebClient kakaoWebClient;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public String createEvent(String accessToken,
                              String title,
                              String description,
                              ZonedDateTime start,
                              ZonedDateTime end,
                              String rrule,
                              int remindMinutes) {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("event_title", title);
        if (description != null) form.add("event_description", description);
        form.add("event_start_at", start.format(ISO));
        form.add("event_end_at", end.format(ISO));
        if (rrule != null && !rrule.isBlank()) form.add("rrule", rrule);
        // 알림: 즉시 울리게 minutes=0 권장 (필요 시 변경)
        form.add("reminders", "[{\"method\":\"ALERT\",\"minutes\":" + remindMinutes + "}]");

        JsonNode resp = kakaoWebClient.post()
                .uri("/v1/api/talkcalendar/create/event")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        // 실제 응답 필드명은 카카오 문서에 맞춰 조정
        return resp != null && resp.has("event_id") ? resp.get("event_id").asText() : null;
    }

    public void updateEvent(String accessToken,
                            String eventId,
                            String title,
                            String description,
                            ZonedDateTime start,
                            ZonedDateTime end,
                            String rrule,
                            Integer remindMinutesOrNull) {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("event_id", eventId);
        if (title != null) form.add("event_title", title);
        if (description != null) form.add("event_description", description);
        if (start != null) form.add("event_start_at", start.format(ISO));
        if (end != null) form.add("event_end_at", end.format(ISO));
        if (rrule != null) form.add("rrule", rrule);
        if (remindMinutesOrNull != null) {
            form.add("reminders", "[{\"method\":\"ALERT\",\"minutes\":" + remindMinutesOrNull + "}]");
        }

        kakaoWebClient.post()
                .uri("/v1/api/talkcalendar/update/event")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void deleteEvent(String accessToken, String eventId) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("event_id", eventId);

        kakaoWebClient.post()
                .uri("/v1/api/talkcalendar/delete/event")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
